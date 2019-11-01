package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpSetting;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.mutable.StringBuilder;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 循環執行 回組發送失敗資料成來源資料格式  > 將TXT檔放到SMS指定位置 > 更新資料狀態
 *
 * @author Kenneth
 */
@Service
public class PnpSMSMsgService {

    private static Logger logger = Logger.getLogger(PnpSMSMsgService.class);
    private CircleEntityManagerControl entityManagerControl;
    private PnpAkkaService pnpAkkaService;
    private PnpRepositoryCustom pnpRepositoryCustom;
    private PNPFtpService pnpFtpService;

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-SMS-Scheduled-%d")
                    .daemon(true).build()
    );
    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public PnpSMSMsgService(CircleEntityManagerControl entityManagerControl, PnpAkkaService pnpAkkaService, PnpRepositoryCustom pnpRepositoryCustom, PNPFtpService pnpFtpService) {
        this.entityManagerControl = entityManagerControl;
        this.pnpAkkaService = pnpAkkaService;
        this.pnpRepositoryCustom = pnpRepositoryCustom;
        this.pnpFtpService = pnpFtpService;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
        if (time == -1) {
            logger.error(" PnpSMSMsgService TimeUnit error :" + time + unit);
            return;
        }
        /* 排程工作 */
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.info(" PnpSMSMsgService startCircle....");

                /* 0: 停止排程) 1: 停止排程，並轉發SMS*/
                int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
                if (1 == bigSwitch || 0 == bigSwitch) {
                    logger.warn("PNP_BIG_SWITCH : " + bigSwitch + " PnpSMSMsgService stop transfer file to SMS FTP ...");
                    return;
                }
                sendingSmsMain();
                sendingSmsMainForDeliveryExpired();
            }
        }, 0, time, TimeUnit.valueOf(unit));
    }

    /**
     * 根據PNP FTP Type 依序發送SMS
     *
     * @see this#startCircle
     */
    public void sendingSmsMain() {
        String processApName = pnpAkkaService.getProcessApName();
        for (PNPFTPType type : PNPFTPType.values()) {
            PnpMain pnpMain;
            try {
                List<? super PnpDetail> details = pnpRepositoryCustom.updateStatusForSms(type, processApName, AbstractPnpMainEntity.STAGE_SMS);
                logger.info("SMS pnpMain details type :" + type + " details size:" + details.size());

                if (CollectionUtils.isEmpty(details)) {
                    logger.info("SMS pnpMain type :" + type + " there is a main has no details!!!");
                    return;
                }

                PnpDetail oneDetail = (PnpDetail) details.get(0);
                pnpMain = pnpRepositoryCustom.findMainByMainId(type, oneDetail.getPnpMainId());
                if (null == pnpMain) {
                    logger.info("SMS pnpMain type :" + type + " not data");
                    return;
                }
                pnpMain.setPnpDetails(details);
                String smsFileName = changeFileName(pnpMain);

                /* 傳檔案到SMS FTP */
                uploadFileToSms(type.getSource(), smsGetTargetStream(type, pnpMain, details), smsFileName);
                //update待發送資料 Status(Sending) & Executor name(hostname)
                updateStatusSuccess(processApName, pnpMain, details);
                pnpAkkaService.tell(pnpMain);
            } catch (Exception e) {
                logger.error(e);
                logger.error("SMS pnpMain type :" + type + " sendingMain error:" + e.getMessage());
            }
        }
    }

    /**
     * 查詢Delivery expire date已過24小時的資料
     * 根據PNPFTPType 依序發送SMS
     */
    private void sendingSmsMainForDeliveryExpired() {
        logger.info("======== Start Check Has Pnp Delivery Expired Data =========");
        String procApName = pnpAkkaService.getProcessApName();
        for (PNPFTPType type : PNPFTPType.values()) {
            try {
                List<? super PnpDetail> details = pnpRepositoryCustom.updateDeliveryExpiredStatus(type, procApName, AbstractPnpMainEntity.STAGE_SMS);


                if (CollectionUtils.isEmpty(details)) {
                    logger.info("Type :" + type + " No Expired Data!!");
                    return;
                }


                logger.info("Type :" + type + " Expired Details Size:" + details.size());

                for (int i = 0, size = details.size(); i < size; i++) {
                    PnpDetail oneDetail = (PnpDetail) details.get(i);
                    PnpMain pnpMain = pnpRepositoryCustom.findMainByMainId(type, oneDetail.getPnpMainId());
                    String smsFileName = changeFileName(pnpMain);
                    pnpMain.setPnpDetails(details);
                    pnpMain.setSmsFileName(smsFileName);
                    //傳檔案到SMS FTP
                    uploadFileToSms(type.getSource(), smsGetTargetStream(type, pnpMain, details), smsFileName);
                    //update待發送資料 Status(Sending) & Executor name(hostname)
                    updateStatusSuccess(procApName, pnpMain, details);
                    pnpAkkaService.tell(pnpMain);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.error("Type :" + type + " sendingMain error:" + e.getMessage());
            }
        }
    }

    /**
     * Chang File Name
     * 發送失敗的資料檔名(O_PRMSMS_250102OCSPENDING_20190624155433000.txt)中時間戳記換為現在的時間後轉發SMS
     *
     * @param pnpMain pnpMain
     * @return After Change File Name
     */
    private String changeFileName(PnpMain pnpMain) {
        String origFileName = pnpMain.getOrigFileName();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        /* 更換檔名(加L) */
        String smsFileName = String.format("%s_L_%s.txt",
                origFileName.substring(0, origFileName.lastIndexOf('_')),
                sdf.format(new Date())
        );

        pnpMain.setSmsFileName(smsFileName);
        logger.info("Ori FileName : " + origFileName);
        logger.info("SMS FileName : " + smsFileName);
        return smsFileName;
    }

    /**
     * 根據類型取得SMS上傳到FTP需寫入的InputStream
     *
     * @param type    PNPFTPType
     * @param pnpMain pnpMain
     * @param details details
     * @return InputStream
     * @throws IOException IOException
     */
    private InputStream smsGetTargetStream(PNPFTPType type, PnpMain pnpMain, List<? super PnpDetail> details) throws IOException {
        logger.info("==================== Convert Encode To BIG5 ====================");
        switch (type) {
            case MING:
                return this.smsMingInputStream((PnpMainMing) pnpMain, details);
            case MITAKE:
                return smsMitakeInputStream((PnpMainMitake) pnpMain, details);
            case EVERY8D:
                return this.smsEvery8dInputStream((PnpMainEvery8d) pnpMain, details);
            case UNICA:
                return this.smsUnicaInputStream((PnpMainUnica) pnpMain, details);
            default:
                return null;
        }
    }


    private InputStream smsMitakeInputStream(PnpMainMitake pnpMain, List<? super PnpDetail> details) {
        /*
         * 三竹 header
         * 欄位            型態       長度     説明
         * GroupID        Varchar    10      群組代號 (TSBANK) 需由三竹簡訊中心設定後方能使用，使用者無法更改。/必填
         * Username       Varchar    20      使用者(帳號)新版簡訊掛帳依據
         * UserPassword   Varchar    10      此欄位(不需填寫)請保留空格(以’&’符號分隔) ex.TSBANK&jack& &2003********00&9999&0
         * OrderTime      Char       14      預約時間 ‘yyyymmddhhmmss’固定14位數簡訊預約時間。簡訊何時送達手機，格式為YYYYMMDDhhmmss
         *                                   # 預約時間大於系統時間，則為預約簡訊。
         *                                   # 預約時間已過或為空白則為即時簡訊。
         *                                   即時簡訊為儘早送出，若受到第6個宵禁欄位的限制，就不一定是立刻送出。
         * ValidityTime   Char       14      有效分鐘數(受限於電信業者：建議勿超過24 H)(只能帶數字)
         * MsgType        Char       1       訊息型態 :宵禁‘0’ 表一般通知簡訊 ->run 09:00~19:00  ‘1’  表警急通知簡訊 ->run 00:00~24:00
         */
        //來源資料HEADER
        StringBuilder header = new StringBuilder();
        header.append(pnpMain.getGroupIDSource() + "&");
        header.append(pnpMain.getUsername() + "_L&");
        header.append(pnpMain.getUserPassword() + "&");
        header.append(pnpMain.getOrderTime() + "&");
        header.append(pnpMain.getValidityTime() + "&");
        header.append(pnpMain.getMsgType() + "\r\n");

        logger.info(header.toString());

        /*
         * 三竹  body
         * 欄位             型態      長度  説明
         * DestCategory    Char      8    "掛帳代碼" =>  PCC Code
         * DestName        Varchar   20   請填入系統有意義之流水號(open端可辯示之唯一序號)
         * DestNo          Varchar   20   手機門號/請填入09帶頭的手機號碼。
         * MsgData         Varchar   333  請勿輸入 % $ ' 字元，不可使用‘&’分隔號，或以全型字使用/簡訊內容。若有換行的需求，請以ASCII Code 6代表換行。必填。
         */

        StringBuilder body = new StringBuilder();

        PnpDetailMitake pnpDetailMitake;
        for (Object detail : details) {
            pnpDetailMitake = (PnpDetailMitake) detail;
            body.append(pnpDetailMitake.getDestCategory() + "&");
            body.append(pnpDetailMitake.getDestName() + "&");
            body.append(pnpDetailMitake.getPhone() + "&");
            body.append(pnpDetailMitake.getMsg() + "\r\n");
        }
        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    private InputStream smsEvery8dInputStream(PnpMainEvery8d pnpMainEvery8d, List<? super PnpDetail> details) throws IOException {

        /*
         * 互動 header
         * 名稱          屬性       長度      Null?    說明
         * Subject      NVARCHAR   200      Y        簡訊主旨
         * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
         * Password     CHAR                Y        使用者密碼(可不填)
         * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
         * ExpireTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
         * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
         * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
         */
        //來源資料HEADER
        StringBuilder header = new StringBuilder();
        header.append(pnpMainEvery8d.getSubject() + "&");
        header.append(pnpMainEvery8d.getUserID() + "_L&");
        header.append(pnpMainEvery8d.getPassword() + "&");
        header.append(pnpMainEvery8d.getOrderTime() + "&");
        header.append(pnpMainEvery8d.getExprieTime() + "&");
        header.append(pnpMainEvery8d.getMsgType() + "&");
        header.append(pnpMainEvery8d.getBatchID() + "\r\n");

        logger.info(header.toString());

        /*
         *   互動 body
         *   名稱          屬性        長度    Null?   說明
         *   SN           char        15     N       名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
         *   DestName     char        36     Y       收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
         *   Mobile       char        20     N       收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
         *   Content      nvarchar    756    N       簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
         *   PID          char        11     Y       身份字號
         *   CampaignID   varchar     28     Y       行銷活動代碼(可為空值)
         *   SegmentID    varchar     10     Y       客群代號(可為空值)
         *   ProgramID    varchar     20     Y       階段代號(可為空值)
         *   Variable1    varchar     15     Y       擴充欄位1(可為空值)
         *   Variable2    varchar     15     Y       擴充欄位2(可為空值)
         */

        StringBuilder body = new StringBuilder();

        PnpDetailEvery8d pnpDetailEvery8d = null;
        for (Object detail : details) {
            pnpDetailEvery8d = (PnpDetailEvery8d) detail;
            body.append(pnpDetailEvery8d.getSn() + "&");
            body.append(pnpDetailEvery8d.getDestName() + "&");
            body.append(pnpDetailEvery8d.getPhone() + "&");
            body.append(pnpDetailEvery8d.getMsg() + "&");
            body.append(pnpDetailEvery8d.getPid() + "&");
            body.append(pnpDetailEvery8d.getCampaignId() + "&");
            body.append(pnpDetailEvery8d.getSegmentId() + "&");
            body.append(pnpDetailEvery8d.getProgramId() + "&");
            body.append(pnpDetailEvery8d.getVariable1() + "&");
            body.append(pnpDetailEvery8d.getVariable2() + "\r\n");
        }

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    private InputStream smsUnicaInputStream(PnpMainUnica pnpMainUnica, List<? super PnpDetail> details) {
        /*
         * 互動 header
         * 名稱          屬性       長度      Null?     說明
         * Subject      NVARCHAR   200      Y        簡訊主旨
         * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
         * Password     CHAR                Y        使用者密碼(可不填)
         * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
         * ExpireTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
         * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
         * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
         */
        //來源資料HEADER
        StringBuilder header = new StringBuilder();
        header.append(pnpMainUnica.getSubject() + "&");
        header.append(pnpMainUnica.getUserID() + "_L&");
        header.append(pnpMainUnica.getPassword() + "&");
        header.append(pnpMainUnica.getOrderTime() + "&");
        header.append(pnpMainUnica.getExprieTime() + "&");
        header.append(pnpMainUnica.getMsgType() + "&");
        header.append(pnpMainUnica.getBatchID() + "\r\n");

        logger.info(header.toString());

        /*
         *   互動 body
         *   名稱        屬性        長度   Null   說明
         *   SN         char        15     N     名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
         *   DestName   char        36     Y     收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
         *   Mobile     char        20     N     收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
         *   Content    nvarchar    756    N     簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
         *   PID        char        11     Y     身份字號
         *   CampaignID varchar     28     Y     行銷活動代碼(可為空值)
         *   SegmentID  varchar     10     Y     客群代號(可為空值)
         *   ProgramID  varchar     20     Y     階段代號(可為空值)
         *   Variable1  varchar     15     Y     擴充欄位1(可為空值)
         *   Variable2  varchar     15     Y     擴充欄位2(可為空值)
         */

        StringBuilder body = new StringBuilder();

        PnpDetailUnica pnpDetailUnica;
        for (Object detail : details) {
            pnpDetailUnica = (PnpDetailUnica) detail;
            body.append(pnpDetailUnica.getSn() + "&");
            body.append(pnpDetailUnica.getDestName() + "&");
            body.append(pnpDetailUnica.getPhone() + "&");
            body.append(pnpDetailUnica.getMsg() + "&");
            body.append(pnpDetailUnica.getPid() + "&");
            body.append(pnpDetailUnica.getCampaignId() + "&");
            body.append(pnpDetailUnica.getSegmentId() + "&");
            body.append(pnpDetailUnica.getProgramId() + "&");
            body.append(pnpDetailUnica.getVariable1() + "&");
            body.append(pnpDetailUnica.getVariable2() + "\r\n");
        }


        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }

    /**
     * 明宣格式
     * 流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
     *
     * @param pnpMainMing pnpMainMing
     * @param details     details
     * @return InputStream
     */
    private InputStream smsMingInputStream(PnpMainMing pnpMainMing, List<? super PnpDetail> details) {
        StringBuilder body = new StringBuilder();

        PnpDetailMing pnpDetailMing;
        for (Object detail : details) {
            pnpDetailMing = (PnpDetailMing) detail;
            body.append(pnpDetailMing.getSn() + ";;");
            body.append(pnpDetailMing.getPhone() + ";;");
            body.append(pnpDetailMing.getMsg() + ";;");
            body.append(pnpDetailMing.getDetailScheduleTime() + ";;");
            body.append(pnpDetailMing.getAccount1() + "_L;;");
            body.append(pnpDetailMing.getAccount2() + "_L;;");
            body.append(pnpDetailMing.getVariable1() + ";;");
            body.append(pnpDetailMing.getVariable2() + ";;");
            body.append(pnpDetailMing.getKeepSecond() + "\r\n");
        }
        return new ByteArrayInputStream((body.toString()).getBytes());
    }


    /**
     * Upload File To SMS
     *
     * @param source       source
     * @param targetStream targetStream
     * @param fileName     fileName
     */
    private void uploadFileToSms(String source, InputStream targetStream, String fileName) {
        logger.info("start uploadFileToSMS ");

        if (targetStream == null) {
            logger.error("SMS uploadFileToSMS error: targetStream is null");
            return;
        }
        PNPFtpSetting setting = pnpFtpService.getFtpSettings(source);

        logger.info(" fileName...." + fileName);

        try {
            pnpFtpService.uploadFileByType(targetStream, fileName, setting.getUploadPath(), setting);
        } catch (Exception e) {
            logger.error(e);
            logger.error("SMS uploadFileToSMS error:" + e.getMessage());
        }
    }

    /**
     * update mains and details status to sending
     *
     * @param processApName processApName
     * @param main          main
     * @param allDetails    allDetails
     */
    private void updateStatusSuccess(String processApName, PnpMain main, List<? super PnpDetail> allDetails) {
        Date now = Calendar.getInstance().getTime();
        if (main != null) {
            List<Object> mains = new ArrayList<>();
            main.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_CHECK_DELIVERY);
            main.setProcApName(processApName);
            main.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
            main.setSmsTime(now);
            main.setModifyTime(now);
            mains.add(main);
            entityManagerControl.merge(mains);
        }
        if (allDetails != null) {
            List<Object> details = new ArrayList<>();
            for (Object detail : allDetails) {
                ((PnpDetail) detail).setSmsFileName(main != null ? main.getSmsFileName() : null);
                ((PnpDetail) detail).setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_CHECK_DELIVERY);
                ((PnpDetail) detail).setSmsStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_CHECK_DELIVERY);
                ((PnpDetail) detail).setSmsTime(now);
                ((PnpDetail) detail).setModifyTime(now);
                details.add(detail);
            }
            if (!details.isEmpty()) {
                entityManagerControl.merge(details);
            }
        }
    }

    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     **/
    @PreDestroy
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            logger.info(" BillingNoticeSendMsgService cancel....");
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            logger.info(" BillingNoticeSendMsgService shutdown....");
            scheduler.shutdown();
        }
    }
}
