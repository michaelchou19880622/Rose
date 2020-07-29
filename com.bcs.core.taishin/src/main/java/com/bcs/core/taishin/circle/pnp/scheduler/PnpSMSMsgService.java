package com.bcs.core.taishin.circle.pnp.scheduler;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainUnicaRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.pnp.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.pnp.ftp.PnpFtpSetting;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.db.repository.EntityManagerControl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.mutable.StringBuilder;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 循環執行 回組發送失敗資料成來源資料格式  > 將TXT檔放到SMS指定位置 > 更新資料狀態
 *
 * @author Kenneth, Alan
 */
@Slf4j(topic = "PnpRecorder")
@Service
public class PnpSMSMsgService {

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-SMS-Scheduled-%d")
                    .daemon(true).build()
    );

    private PNPFtpService pnpFtpService;

    private PnpDetailMitakeRepository pnpDetailMitakeRepository;
    private PnpDetailMingRepository pnpDetailMingRepository;
    private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;
    private PnpDetailUnicaRepository pnpDetailUnicaRepository;

    private PnpMainMitakeRepository pnpMainMitakeRepository;
    private PnpMainMingRepository pnpMainMingRepository;
    private PnpMainEvery8dRepository pnpMainEvery8dRepository;
    private PnpMainUnicaRepository pnpMainUnicaRepository;
    private PnpRepositoryCustom pnpRepositoryCustom;

    /**
     * EntityManagerControl for Persist Insert
     */
    @Autowired
    private EntityManagerControl entityManagerControl;   


    @Autowired
    public PnpSMSMsgService(PNPFtpService pnpFtpService,
                            PnpDetailMitakeRepository pnpDetailMitakeRepository,
                            PnpDetailMingRepository pnpDetailMingRepository,
                            PnpDetailEvery8dRepository pnpDetailEvery8dRepository,
                            PnpDetailUnicaRepository pnpDetailUnicaRepository,
                            PnpMainMitakeRepository pnpMainMitakeRepository,
                            PnpMainMingRepository pnpMainMingRepository,
                            PnpMainEvery8dRepository pnpMainEvery8dRepository,
                            PnpMainUnicaRepository pnpMainUnicaRepository,
                            PnpRepositoryCustom pnpRepositoryCustom) {
        this.pnpFtpService = pnpFtpService;
        this.pnpDetailMitakeRepository = pnpDetailMitakeRepository;
        this.pnpDetailMingRepository = pnpDetailMingRepository;
        this.pnpDetailEvery8dRepository = pnpDetailEvery8dRepository;
        this.pnpDetailUnicaRepository = pnpDetailUnicaRepository;
        this.pnpMainMitakeRepository = pnpMainMitakeRepository;
        this.pnpMainMingRepository = pnpMainMingRepository;
        this.pnpMainEvery8dRepository = pnpMainEvery8dRepository;
        this.pnpMainUnicaRepository = pnpMainUnicaRepository;
        this.pnpRepositoryCustom = pnpRepositoryCustom;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        final String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SEND_SCHEDULE_TIME, true, false);
        if (time == -1) {
            log.warn("Properties [pnp.send.schedule.time] does not found or value is blank. use default value 15s!!");
            time = 15;
        }
        /* 排程工作 */
        scheduler.scheduleWithFixedDelay(this::sendProcess, 0, time, TimeUnit.valueOf(unit));
    }

    private void sendProcess() {
        int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIG_SWITCH, true, false);
        if (bigSwitch == 1 || bigSwitch ==  0) {
            log.warn("Stop sms ftp service!! bigSwitch is : {}", bigSwitch);
            return;
        }
        else if (bigSwitch == -1) {
            log.warn("Can't Load PNP_BIG_SWITCH!");
            return;
        }
        else {
            log.info("BigSwitch is : {}", bigSwitch);        	
        }
        
        Arrays.stream(PnpFtpSourceEnum.values()).forEach(this::sendSmsProcess);
    }

    private void sendSmsProcess(PnpFtpSourceEnum type) {
        try {
            log.info("Start Check Has BC Fail To SMS Data : {}", type);
            
	        // long start = System.currentTimeMillis();
            // long end ;

            /* Find to sms data*/
            List<PnpDetail> detailList = new ArrayList<>();
            
            detailList.addAll(getBcToSmsList(type));
            detailList.addAll(getPnpToSmsList(type));
            detailList.addAll(getPnpExpiredToSmsList(type));

            if (CollectionUtils.isEmpty(detailList)) {
//                log.info("{} does not found to sms data!!", type);
                return;
            }
            
//            log.info("Detail list size is {}\nvalue:{}", detailList.size(), DataUtils.toPrettyJsonUseJackson(detailList));
            log.info("{} type detail list size is {}", type, detailList.size());
            /* Update Expired Status */
            Date now = new Date();
            for (PnpDetail detail : detailList) {
                if (PnpStatusEnum.PNP_SENT_CHECK_DELIVERY.value.equals(detail.getPnpStatus())) {
                    detail.setPnpStatus(PnpStatusEnum.PNP_SENT_EXPIRED_FAIL_SMS_PROCESS.value);
                }
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENDING.value);
                detail.setModifyTime(now);
            }

            // log.info("Before updateDetailStatus()");               	
            List<PnpDetail> afterSaveList = updateDetailStatus(type, detailList);
            //log.info("After updateDetailStatus()");  
            // end = System.currentTimeMillis();
            
            Map<String, InputStream> map = new HashMap<>();
            // Map<Long, PnpMain> pnpMainMap = new HashMap<>();
            //  PnpMain main = null;
            // int i = 0;
            for (PnpDetail detail : afterSaveList) {
                now = new Date();
                detail.setModifyTime(now);
                detail.setDetailScheduleTime(DataUtils.convDateToStr(now, "yyyy-MM-dd HH:mm:ss"));
//                log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(detail));
                log.info(String.format("Before Save Detail id:%s, Status:%s, PnPStatus:%s, BCStatus:%s, SMSStatus:%s ", detail.getPnpDetailId().toString(), detail.getStatus(), detail.getPnpStatus(), detail.getBcStatus(), detail.getSmsStatus()));
            
                /* Find Main By Detail Id -- Use Hash for Cache  */
                 PnpMain main = pnpRepositoryCustom.findSingleMainById(type, detail.getPnpMainId());
                 //TODO : 安排後續效能優化
                /*
//                if ((main = pnpMainMap.get(detail.getPnpMainId())) == null) { 
//	                main = pnpRepositoryCustom.findSingleMainById(type, detail.getPnpMainId());
//	                pnpMainMap.put(detail.getPnpMainId(), main);
//                    log.info("Miss or Don't Hit Main id : {}, i : {} ", detail.getPnpMainId(), i );               	
//                }
//                else {
//                    log.info("Hit Main id : {} , i : {}", detail.getPnpMainId(), i);               	
//                }
//                i++;
                */
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                main.setPnpDetails(Collections.singletonList(detail));
                main.setSmsFileName(smsFileName);
                main.setScheduleTime(DataUtils.convDateToStr(now, "yyyy-MM-dd HH:mm:ss"));
//                pnpMainMap.put(detail.getPnpMainId(), main);
                InputStream inputStream = getInputStream(type, Collections.singletonList(detail), main);
                if (inputStream == null) {
                    log.error("InputStream is null!!");
                    return;
                }
                map.put(smsFileName, inputStream);
            }
            
            /* Send File To SMS FTP */
            uploadFileToSmsWithEvery8dFmt(type, map);
            // i = 0;
            for (PnpDetail detail : afterSaveList) {
            	 PnpMain main = pnpRepositoryCustom.findSingleMainById(type, detail.getPnpMainId());
                /* Find Main By Detail Id -- Cache Policy*/
            	
                //TODO : 安排後續效能優化
            	/*
                if ((main = pnpMainMap.get(detail.getPnpMainId())) == null) { 
	                main = pnpRepositoryCustom.findSingleMainById(type, detail.getPnpMainId());
	                pnpMainMap.put(detail.getPnpMainId(), main);
                    log.info("Not Hit Main id : {}, i : {} ", detail.getPnpMainId(), i );               	
                }
                else {
                    log.info("Hit Main id : {} , i : {}", detail.getPnpMainId(), i);               	
                } 
                */     
                // i++;                
                String smsFileName = changeFileName(main.getOrigFileName(), now);
                /* Save Main */
                main.setProcStage(PnpStageEnum.SMS.value);
                main.setSmsTime(now);
                main.setModifyTime(now);
                PnpMain afterSaveMain = saveMain(type, main);
                //TODO : 安排後續效能優化
//                pnpMainMap.put(detail.getPnpMainId(), main);
//                log.info("After Save Main : {}", DataUtils.toPrettyJsonUseJackson(afterSaveMain));
                log.info(String.format("After Save Main id:%s, Status:%s, ProcStage:%s, ModifyTime:%s ",afterSaveMain.getPnpMainId().toString(), afterSaveMain.getStatus(), afterSaveMain.getProcStage(), afterSaveMain.getModifyTime().toString()));                

                /* Save Detail */
                detail.setSmsFileName(smsFileName);
                detail.setSmsStatus(PnpStatusEnum.SMS_SENT_CHECK_DELIVERY.value);
                detail.setSmsTime(now);
                detail.setModifyTime(now);
                PnpDetail afterSaveDetail = saveDetail(type, detail);
//              log.info("After Save Detail : {}", DataUtils.toPrettyJsonUseJackson(afterSaveDetail));
                log.info(String.format("After Save Detail id:%s, Status:%s, PnPStatus:%s, BCStatus:%s, SMSStatus:%s ",afterSaveDetail.getPnpDetailId().toString(), afterSaveDetail.getStatus(), afterSaveDetail.getPnpStatus(), afterSaveDetail.getBcStatus(), afterSaveDetail.getSmsStatus()));                
            }
            
            // end = System.currentTimeMillis();
            
//            entityManagerControl.persistInsert(details);
            // Cache Persist into DB.
            
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * Resent Sms
     *
     * @param detailId      Detail Id
     * @param ftpSourceEnum Ftp Source Enum
     * @return is Success
     */
    public boolean resendSms(long detailId, PnpFtpSourceEnum ftpSourceEnum) {
        if (ftpSourceEnum == null) {
            return false;
        }
        Date now = new Date();
        PnpDetail detail = pnpRepositoryCustom.findDetailById(ftpSourceEnum, detailId);
        detail.setDetailScheduleTime(DataUtils.convDateToStr(now, "yyyy-MM-dd HH:mm:ss"));
        detail.setModifyTime(now);
        PnpMain main = pnpRepositoryCustom.findSingleMainById(ftpSourceEnum, detail.getPnpMainId());
        main.setScheduleTime(DataUtils.convDateToStr(now, "yyyy-MM-dd HH:mm:ss"));
        main.setPnpDetails(Collections.singletonList(detail));

        /* Change Sms File Name */
        String smsFileName = changeFileName(main.getOrigFileName(), now);
        main.setSmsFileName(smsFileName);
        InputStream inputStream = getInputStream(ftpSourceEnum, Collections.singletonList(detail), main);
        if (inputStream == null) {
            log.error("InputStream is null!!");
            return false;
        }
        /* Send File To SMS FTP */
        uploadFileToSmsWithEvery8dFmt(ftpSourceEnum, Collections.singletonMap(main.getSmsFileName(), inputStream));
        updateDetailStatus(ftpSourceEnum, Collections.singletonList(detail));
        return true;
    }

    private List<PnpDetail> updateDetailStatus(PnpFtpSourceEnum type, List<PnpDetail> detailList) {
        List<PnpDetail> afterSaveList = new ArrayList<>();
        /* Update Status */
        switch (type) {
            case MING:
                List<PnpDetailMing> l1 = detailList.stream().map(detail -> (PnpDetailMing) detail).collect(Collectors.toList());
                List<PnpDetailMing> after1 = pnpDetailMingRepository.save(l1);
                afterSaveList = after1.stream().map(detail -> (PnpDetail) detail).collect(Collectors.toList());
                break;
            case MITAKE:
                List<PnpDetailMitake> l2 = detailList.stream().map(detail -> (PnpDetailMitake) detail).collect(Collectors.toList());
                List<PnpDetailMitake> after2 = pnpDetailMitakeRepository.save(l2);
                afterSaveList = after2.stream().map(detail -> (PnpDetail) detail).collect(Collectors.toList());
                break;
            case UNICA:
                List<PnpDetailUnica> l3 = detailList.stream().map(detail -> (PnpDetailUnica) detail).collect(Collectors.toList());
                List<PnpDetailUnica> after3 = pnpDetailUnicaRepository.save(l3);
                afterSaveList = after3.stream().map(detail -> (PnpDetail) detail).collect(Collectors.toList());
                break;
            case EVERY8D:
                List<PnpDetailEvery8d> l4 = detailList.stream().map(detail -> (PnpDetailEvery8d) detail).collect(Collectors.toList());
                List<PnpDetailEvery8d> after4 = pnpDetailEvery8dRepository.save(l4);
                afterSaveList = after4.stream().map(detail -> (PnpDetail) detail).collect(Collectors.toList());
                break;
            default:
                break;
        }
        return afterSaveList;
    }

    private InputStream getInputStream(PnpFtpSourceEnum type, List<PnpDetail> detailList, PnpMain main) {
        InputStream inputStream = null;
        switch (type) {
            case MING:
//                inputStream = smsMingInputStream(detailList);
                inputStream = smsUnifmtInputStream(type, main, detailList);
                break;
            case UNICA:
                inputStream = smsUnicaInputStream(main, detailList);
                break;
            case MITAKE:
//                inputStream = smsMitakeInputStream(main, detailList);
                inputStream = smsUnifmtInputStream(type, main, detailList);
                break;
            case EVERY8D:
                inputStream = smsEvery8dInputStream(main, detailList);
                break;
            default:
                break;
        }
        return inputStream;
    }

    private PnpMain saveMain(PnpFtpSourceEnum type, PnpMain main) {
        switch (type) {
            case MING:
                return pnpMainMingRepository.save((PnpMainMing) main);
            case MITAKE:
                return pnpMainMitakeRepository.save((PnpMainMitake) main);
            case UNICA:
                return pnpMainUnicaRepository.save((PnpMainUnica) main);
            case EVERY8D:
                return pnpMainEvery8dRepository.save((PnpMainEvery8d) main);
            default:
                return null;
        }
    }

    private PnpDetail saveDetail(PnpFtpSourceEnum type, PnpDetail detail) {
        switch (type) {
            case MING:
                return pnpDetailMingRepository.save((PnpDetailMing) detail);
            case MITAKE:
                return pnpDetailMitakeRepository.save((PnpDetailMitake) detail);
            case UNICA:
                return pnpDetailUnicaRepository.save((PnpDetailUnica) detail);
            case EVERY8D:
                return pnpDetailEvery8dRepository.save((PnpDetailEvery8d) detail);
            default:
                return null;
        }
    }

    private List<PnpDetail> getBcToSmsList(PnpFtpSourceEnum type) {
        List<PnpDetail> list = pnpRepositoryCustom.findDetailByBcStatus(type, Arrays.asList(
                PnpStatusEnum.BC_SENT_FAIL_SMS_PROCESS.value,
                PnpStatusEnum.BC_USER_BLOCKED_SMS_PROCESS.value,
                PnpStatusEnum.BC_UID_NOT_FOUND_SMS_PROCESS.value,
                PnpStatusEnum.BC_USER_IN_BLACK_LIST_SMS_PROCESS.value,
                PnpStatusEnum.USER_IS_UNBIND_IGNORE_TO_SMS.value,
                PnpStatusEnum.USER_IS_SYSTEM_ADD_IGNORE_SMS.value
        ));
        if (!list.isEmpty()) {
            log.info("Bc to sms list size is {}", list.size());
        }
        return list;
    }

    private List<PnpDetail> getPnpToSmsList(PnpFtpSourceEnum type) {
        List<PnpDetail> list = pnpRepositoryCustom.findDetailByPnpStatus(type, Arrays.asList(
                PnpStatusEnum.PNP_SENT_TO_LINE_FAIL_SMS_PROCESS.value,
                PnpStatusEnum.PNP_USER_IN_BLACK_LIST_SMS_PROCESS.value
        ));
        if (!list.isEmpty()) {
            log.info("Pnp to sms list size is {}", list.size());
        }
        return list;
    }

    private List<PnpDetail> getPnpExpiredToSmsList(PnpFtpSourceEnum type) {
        int expiredTime = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME);
        //Default : 預設 PNP_DELIVERY_EXPIRE_TIME轉發至SMS的timeout的時間最少需30分鐘, 目前系統設定值為120分鐘.
        if (expiredTime < 30) {
        	expiredTime = 30;
        }
        
        List<PnpDetail> list = pnpRepositoryCustom.findDetailByPnpStatusAndExpired(type, Collections.singletonList(
                PnpStatusEnum.PNP_SENT_CHECK_DELIVERY.value), expiredTime);
        if (!list.isEmpty()) {
            log.info("Pnp expired to sms list size is {}", list.size());
        }
        return list;
    }


    /**
     * Change File Name (L)
     * 發送失敗的資料檔名 O_PRMSMS_250102OCSPENDING_20190624155433000.txt
     *
     * @param originFileName Origin File Name
     * @param newTime        New Time
     * @return new File Name
     * @apiNote SMS只處理當日資料，因此將檔名中的時間變更為新時間
     */
    private String changeFileName(String originFileName, Date newTime) {
        String smsFileName = String.format("%s_L_%s.txt",
                originFileName.substring(0, originFileName.lastIndexOf('_')),
                DataUtils.formatDateToString(newTime, "yyyyMMddHHmmssSSS"));
        log.info("Ori FileName : " + originFileName);
        log.info("SMS FileName : " + smsFileName);
        return smsFileName;
    }

    /**
     * Mitake
     */
    private InputStream smsMitakeInputStream(PnpMain m, List<PnpDetail> detailList) {
        PnpMainMitake main = (PnpMainMitake) m;
        String tag = "&";
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
        header.append(main.getGroupIDSource() + tag);
        header.append(main.getUsername() + "_L" + tag);
        header.append(main.getUserPassword() + tag);
        header.append(DataUtils.convDateToStr(new Date(), "yyyyMMddHHmmss") + tag);
        header.append(main.getValidityTime() + tag);
        header.append(main.getMsgType() + "\r\n");

        // log.info("Mitake Header: {}", DataUtils.toPrettyJsonUseJackson(header));

        /*
         * 三竹  body
         * 欄位             型態      長度  説明
         * DestCategory    Char      8    "掛帳代碼" =>  PCC Code
         * DestName        Varchar   20   請填入系統有意義之流水號(open端可辯示之唯一序號)
         * DestNo          Varchar   20   手機門號/請填入09帶頭的手機號碼。
         * MsgData         Varchar   333  請勿輸入 % $ ' 字元，不可使用‘&’分隔號，或以全型字使用/簡訊內容。若有換行的需求，請以ASCII Code 6代表換行。必填。
         */

        StringBuilder body = new StringBuilder();

        for (PnpDetail d : detailList) {
            PnpDetailMitake detail = (PnpDetailMitake) d;
            body.append(detail.getDestCategory() + tag);
            body.append(detail.getDestName() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + "\r\n");
        }
        // log.info("Mitake Body: {}", DataUtils.toPrettyJsonUseJackson(header));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    /**
     * Every8d
     */
    private InputStream smsEvery8dInputStream(PnpMain m, List<PnpDetail> detailList) {
        PnpMainEvery8d main = (PnpMainEvery8d) m;
        String tag = "&";
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
        header.append(main.getSubject() + tag);
        header.append(main.getUserID() + "_L" + tag);
        header.append(main.getPassword() + tag);
        header.append(DataUtils.convDateToStr(new Date(), "yyyyMMddHHmmss") + tag);
        header.append(main.getExprieTime() + tag);
        header.append(main.getMsgType() + tag);
        header.append(main.getBatchID() + "\r\n");

        // log.info("Every8d Header: {}", DataUtils.toPrettyJsonUseJackson(header));

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
        for (PnpDetail d : detailList) {
            PnpDetailEvery8d detail = (PnpDetailEvery8d) d;
            body.append(detail.getSn() + tag);
            body.append(detail.getDestName() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + tag);
            body.append(detail.getPid() + tag);
            body.append(detail.getCampaignId() + tag);
            body.append(detail.getSegmentId() + tag);
            body.append(detail.getProgramId() + tag);
            body.append(detail.getVariable1() + tag);
            body.append(detail.getVariable2() + "\r\n");
        }
        // log.info("Every8d body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    /**
     * Unica
     */
    private InputStream smsUnicaInputStream(PnpMain m, List<PnpDetail> detailList) {
        PnpMainUnica main = (PnpMainUnica) m;
        String tag = "&";
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
        header.append(main.getSubject() + tag);
        header.append(main.getUserID() + "_L" + tag);
        header.append(main.getPassword() + tag);
        header.append(DataUtils.convDateToStr(new Date(), "yyyyMMddHHmmss") + tag);
        header.append(main.getExprieTime() + tag);
        header.append(main.getMsgType() + tag);
        header.append(main.getBatchID() + "\r\n");

        // log.info("Unica Header: {}", DataUtils.toPrettyJsonUseJackson(header));

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
        for (PnpDetail d : detailList) {
            PnpDetailUnica detail = (PnpDetailUnica) d;
            body.append(detail.getSn() + tag);
            body.append(detail.getDestName() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + tag);
            body.append(detail.getPid() + tag);
            body.append(detail.getCampaignId() + tag);
            body.append(detail.getSegmentId() + tag);
            body.append(detail.getProgramId() + tag);
            body.append(detail.getVariable1() + tag);
            body.append(detail.getVariable2() + "\r\n");
        }
        // log.info("Unica Body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }

    /**
     * 明宣格式
     *
     * @param detailList detailList
     * @return InputStream
     * @apiNote 流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
     */
    private InputStream smsMingInputStream(List<PnpDetail> detailList) {
        StringBuilder body = new StringBuilder();
        String tag = ";;";
        for (PnpDetail d : detailList) {
            PnpDetailMing detail = (PnpDetailMing) d;
            body.append(detail.getSn() + tag);
            body.append(detail.getPhone() + tag);
            body.append(detail.getMsg() + tag);
            body.append(detail.getDetailScheduleTime() + tag);
            body.append(detail.getAccount1() + "_L" + tag);
            body.append(detail.getAccount2() + "_L" + tag);
            body.append(detail.getVariable1() + tag);
            body.append(detail.getVariable2() + tag);
            body.append(detail.getKeepSecond() + "\r\n");
        }
        // log.info("Ming Body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((body.toString()).getBytes());
    }

    /**
     * Every8d 通用專換格式，依據type 取得對應欄位值進行轉換
     *
     * @param detailList detailList
     * @return InputStream
     * @apiNote 流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
     */
    private InputStream smsUnifmtInputStream(PnpFtpSourceEnum type, PnpMain m, List<PnpDetail> detailList) {
        //PnpMainEvery8d main; = (PnpMainEvery8d) m;
        String tag = "&";
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
        switch (type) {
            case MING:
                PnpDetailMing detail = (PnpDetailMing) detailList.get(0);
                header.append("" + tag);
                header.append(detail.getAccount1() + "_L" + tag);
                header.append("" + tag);
                header.append(DataUtils.convDateToStr(new Date(), "yyyyMMddHHmmss") + tag);
                header.append("" + tag);
                header.append("1" + tag);
                header.append("" + "\r\n");
                break;
            case MITAKE:
                PnpMainMitake main = (PnpMainMitake) m;
                header.append("" + tag);
                header.append(main.getUsername() + "_L" + tag);
                header.append(main.getUserPassword() + tag);
                header.append(DataUtils.convDateToStr(new Date(), "yyyyMMddHHmmss") + tag);
                header.append("" + tag);
                header.append(main.getMsgType() + tag);
                header.append("" + "\r\n");
                break;
            default:
        }




        // log.info("Every8d Header: {}", DataUtils.toPrettyJsonUseJackson(header));

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
        for (PnpDetail d : detailList) {
            switch (type) {
                case MING:
                    PnpDetailMing detail = (PnpDetailMing) d;
                    body.append(detail.getSn() + tag);
                    body.append("" + tag);
                    body.append(detail.getPhone() + tag);
                    body.append(detail.getMsg() + tag);
                    body.append("" + tag);
                    body.append("" + tag);
                    body.append("" + tag);
                    body.append("" + tag);
                    body.append(detail.getVariable1() + tag);
                    body.append(detail.getVariable2() + "\r\n");
                    break;
                case MITAKE:
                    PnpDetailMitake detailMitake = (PnpDetailMitake) d;

                    body.append(detailMitake.getDestCategory() + tag);
                    body.append(""+ tag);
                    body.append(detailMitake.getPhone() + tag);
                    body.append(detailMitake.getMsg() + tag);
                    body.append("" + tag);
                    body.append("" + tag);
                    body.append("" + tag);
                    body.append("" + tag);
                    body.append(detailMitake.getVariable1() + tag);
                    body.append(detailMitake.getVariable2() + "\r\n");
                    break;
                default:
            }
        }
        // log.info("Every8d body: {}", DataUtils.toPrettyJsonUseJackson(body));

        return new ByteArrayInputStream((header.toString() + body.toString()).getBytes());
    }


    /**
     * Upload File To SMS
     */
    private void uploadFileToSms(PnpFtpSourceEnum source, Map<String, InputStream> targetStreamMap) {
        log.info("Start Upload File To SMS!!");

        PnpFtpSetting setting = pnpFtpService.getFtpSettings(source);
        if (StringUtils.isBlank(setting.getAccount())
                || StringUtils.isBlank(setting.getPassword())) {
            log.error("FTP account or password is blank!!");
            return;
        }
        try {
            pnpFtpService.uploadFileByType(targetStreamMap, setting.getUploadPath(), setting);

        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
    /**
     * Upload File To SMS With Every8d Format
     */
    private void uploadFileToSmsWithEvery8dFmt(PnpFtpSourceEnum source, Map<String, InputStream> targetStreamMap) {
        log.info("Start Upload File To SMS With Every8d Format.");

        final PnpFtpSetting setting = pnpFtpService.getFtpSettings(source);
        if (StringUtils.isBlank(setting.getAccount())
                || StringUtils.isBlank(setting.getPassword())) {
            log.error("FTP account or password is blank!!");
            return;
        }
        try {
            pnpFtpService.uploadFileByType(targetStreamMap, setting.getUploadToEvery8dPath(), setting);
        } catch (Exception e) {
            log.error("Exception : {}", e);
        }
    }


    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            log.info("Shutdown....");
            scheduler.shutdown();
        }
    }
}