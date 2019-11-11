package com.bcs.core.taishin.circle.PNP.service;

import akka.actor.ActorRef;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.akka.handler.PnpMessageActor;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDeliveryRecord;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpFlexTemplate;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpFlexTemplateRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainUnicaRepository;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.RestfulUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * @author ???
 * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpTask#execute
 */
@Service
public class PnpService {
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(PnpService.class);
    @Autowired
    private PnpMainMitakeRepository pnpMainMitakeRepository;
    @Autowired
    private PnpDetailMitakeRepository pnpDetailMitakeRepository;
    @Autowired
    private PnpMainEvery8dRepository pnpMainEvery8dRepository;
    @Autowired
    private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;
    @Autowired
    private PnpMainUnicaRepository pnpMainUnicaRepository;
    @Autowired
    private PnpDetailUnicaRepository pnpDetailUnicaRepository;
    @Autowired
    private PnpMainMingRepository pnpMainMingRepository;
    @Autowired
    private PnpDetailMingRepository pnpDetailMingRepository;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private PnpAkkaService pnpAkkaService;

    @Autowired
    private PnpFlexTemplateRepository pnpFlexTemplateRepository;

    /**
     * Save By Source Type
     *
     * @param pnpDetail pnpDetail
     * @return 儲存完成後物件
     * @see com.bcs.core.taishin.circle.PNP.akka.handler.PnpUpdateStatusActor#onReceive
     */
    public PnpDetail saveBySourceType(Object pnpDetail) {
        String source = ((PnpDetail) pnpDetail).getSource();
        if (StringUtils.isBlank(source)) {
            logger.error("PnpService save getting source is blank!!! MainID :" + ((PnpDetail) pnpDetail).getPnpMainId());
        }

        switch (source) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
                return saveMitakeDetailStatus((PnpDetailMitake) pnpDetail);
            case AbstractPnpMainEntity.SOURCE_MING:
                return saveMingDetailStatus((PnpDetailMing) pnpDetail);
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                return saveEvery8dDetailStatus((PnpDetailEvery8d) pnpDetail);
            case AbstractPnpMainEntity.SOURCE_UNICA:
                return saveUnicaDetailStatus((PnpDetailUnica) pnpDetail);
            default:
                return null;
        }
    }

    /**
     * Save Detail SendTime
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#saveBySourceType
     */
    private PnpDetail saveMitakeDetailStatus(PnpDetailMitake pnpDetail) {
        String status = pnpDetail.getStatus();
        if (status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_PNP_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
            logger.info(String.format("Update SendTime: %s, Status: %s", pnpDetail.getSendTime(), status));
        }
        pnpDetail.setModifyTime(new Date());
        logger.info(String.format("Before Save Detail:%n%s", pnpDetail.toString()));
        return pnpDetailMitakeRepository.save(pnpDetail);

    }

    /**
     * Save Detail SendTime
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#saveBySourceType
     */
    private PnpDetail saveEvery8dDetailStatus(PnpDetailEvery8d pnpDetail) {
        String status = pnpDetail.getStatus();
        if (status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_PNP_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
            logger.info(String.format("Update SendTime: %s, Status: %s", pnpDetail.getSendTime(), status));
        }
        pnpDetail.setModifyTime(new Date());
        logger.info(String.format("Before Save Detail:%n%s", pnpDetail.toString()));
        return pnpDetailEvery8dRepository.save(pnpDetail);

    }

    /**
     * Save Detail SendTime
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#saveBySourceType
     */
    private PnpDetail saveUnicaDetailStatus(PnpDetailUnica pnpDetail) {
        String status = pnpDetail.getStatus();
        if (status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_PNP_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
            logger.info(String.format("Update SendTime: %s, Status: %s", pnpDetail.getSendTime(), status));
        }
        pnpDetail.setModifyTime(new Date());
        logger.info(String.format("Before Save Detail:%n%s", pnpDetail.toString()));
        return pnpDetailUnicaRepository.save(pnpDetail);

    }

    /**
     * Save Detail SendTime
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#saveBySourceType
     */
    private PnpDetail saveMingDetailStatus(PnpDetailMing pnpDetail) {
        String status = pnpDetail.getStatus();
        if (status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE)
                || status.equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_PNP_COMPLETE)) {
            // FIXME PNP CHECK_DELIVERY 接收Line回傳訊息不是呼叫這隻Method處理，於是PNP_COMPLETE根本不會進行
            // TODO 須將com.bcs.core.bot/src/main/java/com/bcs/core/bot/db/service/MsgBotReceiveService.java
            //  透過APi打到這邊讓Akka處理
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
            logger.info(String.format("Update SendTime: %s, Status: %s", pnpDetail.getSendTime(), status));
        }
        pnpDetail.setModifyTime(new Date());
        logger.info(String.format("Before Save Detail:%n%s", pnpDetail.toString()));
        return pnpDetailMingRepository.save(pnpDetail);

    }

    /**
     * 檢查訊息狀態並更新狀態為Complete
     *
     * @param mainId    Detail ID
     * @param source    Source
     * @param procStage BC PNP SMS
     * @see com.bcs.core.taishin.circle.PNP.akka.handler.PnpUpdateStatusActor#onReceive
     */
    public void updatePnpMainStatusComplete(Long mainId, String source, String procStage) {
        if (StringUtils.isBlank(source)) {
            logger.error("PnpService save getting source is blank!!! MainID :" + mainId);
        }

        /* 不可更新Main為Complete的Detail狀態清單 */
        List<String> status = new ArrayList<>();
        /* FTP */
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_SCHEDULED);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);

        /* BC */
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_PROCESS);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_SENDING);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_PNP_PROCESS);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_SMS_PROCESS);

        /* PNP */
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PNP_SENDING);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_CHECK_DELIVERY);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PNP_FAIL_SMS_PROCESS);

        /* SMS */
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_CHECK_DELIVERY);


        switch (source) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
                updatePnpMainMitakeStatusComplete(mainId, status, procStage);
                break;
            case AbstractPnpMainEntity.SOURCE_MING:
                updatePnpMainMingStatusComplete(mainId, status, procStage);
                break;
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                updatePnpMainEvery8dStatusComplete(mainId, status, procStage);
                break;
            case AbstractPnpMainEntity.SOURCE_UNICA:
                updatePnpMainUnicaStatusComplete(mainId, status, procStage);
                break;
            default:
                break;
        }
    }

    /**
     * 三竹格式
     * 若明細已無重試或等待發送/排程中或者傳遞中的狀態資料，則更新主檔狀態為Complete及modifyTime
     *
     * @param mainId 訊息ID
     * @param status 訊息狀態清單
     * @see this#updatePnpMainStatusComplete
     */
    private void updatePnpMainMitakeStatusComplete(Long mainId, List<String> status, String procStage) {
        if (pnpDetailMitakeRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            int returnInt = pnpMainMitakeRepository.updatePnpMainMitakeStatus(
                    getCompleteStatusByStage(procStage), new Date(), mainId
            );
            logger.info("After Main Save Return int is : " + returnInt);
        }
    }

    /**
     * 互動格式
     * 若明細已無重試或等待發送/排程中或者傳遞中的狀態資料，則更新主檔狀態為Complete及modifyTime
     *
     * @param mainId 訊息ID
     * @param status 訊息狀態清單
     * @see this#updatePnpMainStatusComplete
     */
    private void updatePnpMainEvery8dStatusComplete(Long mainId, List<String> status, String procStage) {
        if (pnpDetailEvery8dRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            int returnInt = pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(
                    getCompleteStatusByStage(procStage), new Date(), mainId
            );
            logger.info("After Main Save Return int is : " + returnInt);
        }
    }

    /**
     * UNICA格式
     * 若明細已無重試或等待發送/排程中或者傳遞中的狀態資料，則更新主檔狀態為Complete及modifyTime
     *
     * @param mainId 訊息ID
     * @param status 訊息狀態清單
     * @see this#updatePnpMainStatusComplete
     */
    private void updatePnpMainUnicaStatusComplete(Long mainId, List<String> status, String procStage) {
        if (pnpDetailUnicaRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            int returnInt = pnpMainUnicaRepository.updatePnpMainUnicaStatus(
                    getCompleteStatusByStage(procStage), new Date(), mainId
            );
            logger.info("After Main Save Return int is : " + returnInt);
        }
    }

    /**
     * 明宣格式
     * 若明細已無重試或等待發送/排程中或者傳遞中的狀態資料，則更新主檔狀態為Complete及modifyTime
     *
     * @param mainId 訊息ID
     * @param status 訊息狀態清單
     * @see this#updatePnpMainStatusComplete
     */
    private void updatePnpMainMingStatusComplete(Long mainId, List<String> status, String procStage) {
        if (pnpDetailMingRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            int returnInt = pnpMainMingRepository.updatePnpMainMingStatus(
                    getCompleteStatusByStage(procStage), new Date(), mainId
            );
            logger.info("After Main Save Return int is : " + returnInt);
        }
    }

    /**
     * 依照BC、PNP取得各自的Complete Status
     *
     * @param procStage BC PNP SMS
     * @return 各自的Complete Status
     */
    private String getCompleteStatusByStage(String procStage) {
        logger.info("ProcStage: " + procStage);
        switch (procStage) {
            case "BC":
                return AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE;
            case "PNP":
                return AbstractPnpMainEntity.DATA_CONVERTER_STATUS_PNP_COMPLETE;
            case "SMS":
                return AbstractPnpMainEntity.DATA_CONVERTER_STATUS_SMS_COMPLETE;
            default:
                return null;
        }
    }

    /**
     * Update PnpMain and PnpDetail Status
     *
     * @param pnpMain pnpMain
     * @param status  status
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpTaskService#startTask 批次排程開始將進入排程的訊息狀態更改為Delay
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void updateMainAndDetailStatus(PnpMain pnpMain, String status) {
        List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();
        for (PnpDetail detail : details) {
            detail.setStatus(status);
            saveBySourceType(detail);
        }
        Date now = Calendar.getInstance().getTime();

        String source = pnpMain.getSource();

        switch (source) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
                pnpMainMitakeRepository.updatePnpMainMitakeStatus(status, now, pnpMain.getPnpMainId());
                break;
            case AbstractPnpMainEntity.SOURCE_MING:
                pnpMainMingRepository.updatePnpMainMingStatus(status, now, pnpMain.getPnpMainId());
                break;
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(status, now, pnpMain.getPnpMainId());
                break;
            case AbstractPnpMainEntity.SOURCE_UNICA:
                pnpMainUnicaRepository.updatePnpMainUnicaStatus(status, now, pnpMain.getPnpMainId());
                break;
            default:
                break;
        }
        logger.info("Status is Updated to Delay!!");
    }

    /**
     * BC 訊息推播
     * Use REST Template Send BC Message To Line
     *
     * @param pnpMain      pnpMain
     * @param sendRef      sendRef
     * @param selfActorRef selfActorRef
     * @see com.bcs.core.taishin.circle.PNP.akka.handler.PnpPushMessageActor#onReceive 排程時間過期立即執行
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpTask#execute 排程執行判斷為PNP
     */
    @SuppressWarnings("unchecked")
    public void pushLineMessage(PnpMain pnpMain, ActorRef sendRef, ActorRef selfActorRef) {

        String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
        String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
                CONFIG_STR.ChannelToken.toString(), true);
        String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(),
                CONFIG_STR.ChannelServiceCode.toString(), true);

        /* 設定 request headers */
        HttpHeaders headers = getLineApiHttpHeaders(accessToken, serviceCode);
        List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();

        logger.info("pushLineMessage pnpMain.getProcessFlow():" + pnpMain.getProcFlow());

        boolean sendSuccessFlag;
        for (PnpDetail detail : details) {
            if (StringUtils.isBlank(detail.getUid())) {
                /* UID is Empty 轉發 PNP */
                sendSuccessFlag = false;
                logger.info("Line UID Not Found in Detail!! =>  PNP!!  " + " Main Id: " + detail.getPnpMainId() + " Detail Id: " + detail.getPnpDetailId());
            } else {
                /* 發送訊息 */
                sendSuccessFlag = pnpPushMessage(url, headers, detail, detail.getUid());
            }

            if (sendSuccessFlag) {
                /* 發送成功 */
                logger.info("BC Send Message Success!!");
                detail.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE);
                detail.setBcStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_BC_COMPLETE);
                detail.setLinePushTime(Calendar.getInstance().getTime());
            } else {
                /* 發送失敗 */
                logger.warn("BC Send Message Fail!!");
                /* FIXME 20190822 Record ReturnCode*/
                String processFlow = pnpMain.getProcFlow();
                detail.setLinePushTime(Calendar.getInstance().getTime());
                switch (processFlow) {
                    case AbstractPnpMainEntity.PROC_FLOW_BC:
                        detail.setProcStage(AbstractPnpMainEntity.STAGE_BC);
                        detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_FINISH);
                        detail.setBcStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL);
                        break;
                    case AbstractPnpMainEntity.PROC_FLOW_BC_SMS:
                        detail.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
                        detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
                        detail.setBcStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_SMS_PROCESS);
                        break;
                    case AbstractPnpMainEntity.PROC_FLOW_BC_PNP_SMS:
                        detail.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
                        detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
                        detail.setBcStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_PNP_PROCESS);
                        break;
                    default:
                        break;
                }
                logger.info(String.format("Process Flow: %s, After Stage: %s, After Status: %s",
                        detail.getProcFlow(), detail.getProcStage(), detail.getStatus()));
            }
            if (sendRef != null) {
                sendRef.tell(detail, selfActorRef);
            } else {
                pnpAkkaService.tell(detail);
            }
        }
    }

    /**
     * PNP 訊息推播
     * Use REST Template Send PNP Message To Line
     *
     * @param pnpMain      pnpMain
     * @param sendRef      sendRef
     * @param selfActorRef selfActorRef
     * @see PnpMessageActor#immediatePushMessage 排程時間過期立即執行
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpTask#execute 排程執行判斷為PNP
     */
    @SuppressWarnings("unchecked")
    public void pushPnpMessage(PnpMain pnpMain, ActorRef sendRef, ActorRef selfActorRef) {
        try {
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED.toString());
            String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
                    CONFIG_STR.ChannelToken.toString(), true);
            String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(),
                    CONFIG_STR.ChannelServiceCode.toString(), true);
            HttpHeaders headers = getLineApiHttpHeaders(accessToken, serviceCode);

            List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();

            String source = pnpMain.getSource();

            boolean sendSuccessFlag;
            for (PnpDetail detail : details) {

                String deliveryTag = formatMessageToLineDeliveryTag(source, detail);
                headers.set("X-Line-Delivery-Tag", deliveryTag);
                logger.debug("X-Line-Delivery-Tag : " + deliveryTag);

                /* 發送訊息 */
                sendSuccessFlag = pnpPushMessage(url, headers, detail, detail.getPhoneHash());
                Date pnpSendTime = new Date();
                detail.setPnpTime(pnpSendTime);
                if (sendSuccessFlag) {
                    /* 發送成功 */
                    logger.info("PNP Send Message Success!!");
                    //設定PnpDeliveryExpireTime,SMS排程將抓取status = CHECK_DELIVERY 且 now date > PnpDeliveryExpireTime 的資料

                    int expiredUnit = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME_UNIT, true, false);
                    int expired = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME, true, false);
                    logger.info(String.format("expired: %s, expiredUnit: %s", expired, expiredUnit));

                    /* 設定PnpDeliveryExpireTime */
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(pnpSendTime);
                    calendar.add(expiredUnit, expired);
                    detail.setPnpDeliveryExpireTime(calendar.getTime());
                    logger.info("Pnp Send Time            : " + DataUtils.formatDateToString(pnpSendTime, "yyyy-MM-dd HH:mm:ss"));
                    logger.info("Pnp Delivery Expire Time : " + DataUtils.formatDateToString(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
                    //待web hook在24小時內收到DELIVERY則將該則訊息update成COMPLETE，若24小時內沒收到DELIVERY則將該訊息轉發SMS
                    detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_CHECK_DELIVERY);
                    detail.setPnpStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_CHECK_DELIVERY);
                } else {
                    /* 發送失敗 */
                    logger.info("PNP Send Message Fail!! ==> SMS!!");
                    detail.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
                    detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
                    detail.setPnpStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PNP_FAIL_SMS_PROCESS);
                }
                logger.info(String.format("Process Flow: %s, After Proc Stage: %s, After Status: %s"
                        , detail.getProcFlow(), detail.getProcStage(), detail.getStatus()));
                if (sendRef != null) {
                    logger.info("Tell SendRef: " + sendRef);
                    sendRef.tell(detail, selfActorRef);
                } else {
                    logger.info("Tell PnpAkkaService to Update Status");
                    pnpAkkaService.tell(detail);
                }
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }

    /**
     * 設定 request headers
     *
     * @see this#pushLineMessage(PnpMain, ActorRef, ActorRef) BC Push
     * @see this#pushPnpMessage(PnpMain, ActorRef, ActorRef)  PNP Push
     **/
    private HttpHeaders getLineApiHttpHeaders(String accessToken, String serviceCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
        return headers;
    }

    /**
     * BC PNP 通用發送推播訊息程序
     *
     * @param url     Line API URL
     * @param headers request header
     * @param detail  went push message object
     * @return Push is success
     * @see this#pushLineMessage(PnpMain, ActorRef, ActorRef) BC Push
     */
    private boolean bcPushMessage(String url, HttpHeaders headers, PnpDetail detail) {
        boolean sendSuccessFlag;
        JSONObject requestBody = new JSONObject();

        requestBody.put("to", detail.getUid());
        requestBody.put("messages", combineLineTextMessage(detail.getMsg()));
        logger.info("BC Push RequestBody : " + requestBody.toString());
        logger.info("BC Push RequestBody : " + DataUtils.toPrettyJsonUseJackson(requestBody.toString()));

        /* 將 headers 跟 body 塞進 HttpEntity 中 */
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);
        try {
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            restfulUtil.execute();
            logger.info("RestfulUtil.getStatusCode: " + restfulUtil.getStatusCode());
            sendSuccessFlag = "200".equals(restfulUtil.getStatusCode());
        } catch (HttpClientErrorException he) {
            sendSuccessFlag = false;
            logger.error("HttpClientErrorException error : " + he.getMessage());
            JSONObject errorMessage = new JSONObject(he.getResponseBodyAsString());
            if (errorMessage.has("message")) {
                logger.error("HttpClientErrorException statusCode: " + he.getStatusCode().toString());
                if (errorMessage.has("details")) {
                    logger.error("HttpClientErrorException details : " + errorMessage.getJSONArray("details").toString());
                }
            }
        } catch (Exception e) {
            sendSuccessFlag = false;
            logger.info("Send fail PnpDetailId:" + detail.getPnpDetailId());
            logger.error("Send fail Exception:" + e.getMessage());
        }
        return sendSuccessFlag;
    }

    /**
     * BC PNP 通用發送推播訊息程序
     *
     * @param url     Line API URL
     * @param headers request header
     * @param detail  went push message object
     * @param to      UID Or PhoneNumber
     * @return Push is success
     * @see this#pushPnpMessage(PnpMain, ActorRef, ActorRef)  PNP Push
     */
    private boolean pnpPushMessage(String url, HttpHeaders headers, PnpDetail detail, String to) {
        boolean sendSuccessFlag;
        JSONObject requestBody = new JSONObject();

        requestBody.put("to", to);
        String message = combineLineFlexMessage(detail.getMsg(), detail.getFlexTemplateId());
        if (message == null) {
            /* 舊有PNP純文字發送 */
            requestBody.put("messages", combineLineTextMessage(detail.getMsg()));
        } else {
            /* Flex 新樣板 */
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(new JSONObject(message));
            requestBody.put("messages", jsonArray);
        }
        logger.info("Pnp Push RequestBody : " + requestBody.toString());

        logger.info("Pnp Push RequestBody : " + DataUtils.toPrettyJsonUseJackson(requestBody.toString()));

        /* 將 headers 跟 body 塞進 HttpEntity 中 */
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);
        try {
            RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
            restfulUtil.execute();
            logger.info("RestfulUtil.getStatusCode: " + restfulUtil.getStatusCode());
            sendSuccessFlag = "200".equals(restfulUtil.getStatusCode());
        } catch (HttpClientErrorException he) {
            sendSuccessFlag = false;
            logger.error("HttpClientErrorException error : " + he.getMessage());
            JSONObject errorMessage = new JSONObject(he.getResponseBodyAsString());
            if (errorMessage.has("message")) {
                logger.error("HttpClientErrorException statusCode: " + he.getStatusCode().toString());
                if (errorMessage.has("details")) {
                    logger.error("HttpClientErrorException details : " + errorMessage.getJSONArray("details").toString());
                }
            }
        } catch (Exception e) {
            sendSuccessFlag = false;
            logger.info("Send fail PnpDetailId:" + detail.getPnpDetailId());
            logger.error("Send fail Exception:" + e.getMessage());
        }
        return sendSuccessFlag;
    }

    /**
     * 格式化訊息物件成特殊格式
     * "RecordType;;Source;;MainId;;DetailID;;PhoneHash"
     *
     * @param source source
     * @param detail detail
     * @return String 格式化字串
     */
    private String formatMessageToLineDeliveryTag(String source, PnpDetail detail) {
        String pnpMessage = String.format("%s;;%s;;%s;;%s;;%s",
                PnpDeliveryRecord.THIS_TYPE, source,
                detail.getPnpMainId().toString(),
                detail.getPnpDetailId().toString(),
                detail.getPhoneHash());

        // 64 <= tag length <= 100，不夠則補空格
        return String.format("%1$-" + 64 + "s", pnpMessage);
    }

    /**
     * 將欲傳遞至Line的訊息格式化成Line格式
     * <p>
     * "messages":[
     * {"type":"text","text":"Hello, world1"},
     * {"type":"text","text":"Hello, world2"}
     * ]
     * <p>
     * Max Messages : 5
     *
     * @param msg message
     * @return JSONArray JSONArray
     */
    private JSONArray combineLineTextMessage(String msg) {
        JSONArray messageArray = new JSONArray();
        JSONObject messageMap = new JSONObject();
        messageMap.put("type", "text");
        messageMap.put("text", msg);
        messageArray.put(messageMap);

        /* 是否啟用加掛公告訊息 */
        boolean isDeclarationEnable = CoreConfigReader.getBoolean("pnp.push.declaration.enable", true);
        String declarationMessage = CoreConfigReader.getString("pnp.push.declaration.message", true);
        logger.info("isDeclarationEnable : " + isDeclarationEnable);
        logger.info("declarationMessage  : " + declarationMessage);
        if (isDeclarationEnable && StringUtils.isNotBlank(declarationMessage)) {
            JSONObject declarationMsgMap = new JSONObject();
            declarationMsgMap.put("type", "text");
            declarationMsgMap.put("text", declarationMessage);
            messageArray.put(declarationMsgMap);
        }
        return messageArray;
    }

    private String combineLineFlexMessage(String msg, String templateId) {
        logger.info("Msg : " + msg + "Template Id: " + templateId);
        Long id;
        try {
            id = Long.parseLong(templateId);
        } catch (NumberFormatException e) {
            // TODO 採取預設樣板
            return null;
        }
        String footerUrl = CoreConfigReader.getString("flex.ui.footer.link.url", true);

        PnpFlexTemplate pnpFlexTemplate = pnpFlexTemplateRepository.findOne(id);
        String templateJson = PnpFlexTemplate.fetchDefaultTemplateJson()
                .replace("headerBackground", pnpFlexTemplate.getHeaderBackground().trim())
                .replace("headerTextSize", pnpFlexTemplate.getHeaderTextSize().trim())
                .replace("headerTextColor", pnpFlexTemplate.getHeaderTextColor().trim())
                .replace("headerTextWeight", pnpFlexTemplate.getHeaderTextWeight().trim())
                .replace("headerTextStyle", pnpFlexTemplate.getHeaderTextStyle().trim())
                .replace("headerTextDecoration", pnpFlexTemplate.getHeaderTextDecoration().trim())
                .replace("headerText", pnpFlexTemplate.getHeaderText().trim())


                .replace("heroBackground", pnpFlexTemplate.getHeroBackground().trim())
                .replace("heroTextSize", pnpFlexTemplate.getHeroTextSize().trim())
                .replace("heroTextColor", pnpFlexTemplate.getHeroTextColor().trim())
                .replace("heroTextWeight", pnpFlexTemplate.getHeroTextWeight().trim())
                .replace("heroTextStyle", pnpFlexTemplate.getHeroTextStyle().trim())
                .replace("heroTextDecoration", pnpFlexTemplate.getHeroTextDecoration().trim())
                .replace("heroText", msg.trim())


                .replace("bodyDescTextSize", pnpFlexTemplate.getBodyDescTextSize().trim())
                .replace("bodyDescTextColor", pnpFlexTemplate.getBodyDescTextColor().trim())
                .replace("bodyDescTextWeight", pnpFlexTemplate.getBodyDescTextWeight().trim())
                .replace("bodyDescTextStyle", pnpFlexTemplate.getBodyDescTextStyle().trim())
                .replace("bodyDescTextDecoration", pnpFlexTemplate.getBodyDescTextDecoration().trim())
                .replace("bodyBackground", pnpFlexTemplate.getBodyBackground().trim())
                .replace("bodyDescText", pnpFlexTemplate.getBodyDescText().trim())


                .replace("footerLinkText", pnpFlexTemplate.getFooterLinkText().trim())


                .replaceAll("footerLinkUrl", "".equals(footerUrl) ? pnpFlexTemplate.getFooterLinkUrl() : footerUrl.trim());

        StringBuilder sb = new StringBuilder();
        String[] buttonTextArray = pnpFlexTemplate.getButtonText().split(",");
        String[] buttonUrlArray = pnpFlexTemplate.getButtonUrl().split(",");
        String[] buttonColorArray = pnpFlexTemplate.getButtonColor().split(",");

        for (int i = 0; i < buttonTextArray.length; i++) {
            String url;
            String color;
            String text;
            try {
                url = buttonUrlArray[i];
            } catch (IndexOutOfBoundsException e) {
                url = "";
            }
            try {
                color = buttonColorArray[i];
            } catch (IndexOutOfBoundsException e) {
                color = "";
            }
            try {
                text = buttonTextArray[i];
            } catch (IndexOutOfBoundsException e) {
                text = "";
            }

            /* Check button parameter is not empty, Require by Line API!! */
            if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(color) && StringUtils.isNotBlank(text)) {
                sb.append(PnpFlexTemplate.fetchDefaultButtonTemplateJson()
                        .replace("bodyButtonText", text.trim())
                        .replace("bodyLinkUrl", url.trim())
                        .replace("bodyButtonColor", color).trim());
            }
        }
        templateJson = templateJson.replace("buttonJsonArea", sb.toString());
        logger.info("final templateJson: " + DataUtils.toPrettyJsonUseJackson(templateJson));
        return templateJson;
    }
}
