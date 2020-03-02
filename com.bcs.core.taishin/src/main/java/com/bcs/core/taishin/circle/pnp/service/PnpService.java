package com.bcs.core.taishin.circle.pnp.service;

import akka.actor.ActorRef;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.akka.handler.PnpMessageActor;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpProcessFlowEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDeliveryRecord;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpFlexTemplate;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpSendBlock;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpFlexTemplateRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpMainUnicaRepository;
import com.bcs.core.taishin.circle.pnp.db.service.PnpSendBlockService;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.RestfulUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.client.HttpServerErrorException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author ???
 * @see com.bcs.core.taishin.circle.pnp.scheduler.PnpTask#execute
 */
@Slf4j(topic = "PnpRecorder")
@Service
public class PnpService {
    private static final int DEFAULT_SLEEP_TIME = 5_000;
    private static final int DEFAULT_RETRY_COUNT = 3;

    private PnpMainMitakeRepository pnpMainMitakeRepository;
    private PnpDetailMitakeRepository pnpDetailMitakeRepository;
    private PnpMainEvery8dRepository pnpMainEvery8dRepository;
    private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;
    private PnpMainUnicaRepository pnpMainUnicaRepository;
    private PnpDetailUnicaRepository pnpDetailUnicaRepository;
    private PnpMainMingRepository pnpMainMingRepository;
    private PnpDetailMingRepository pnpDetailMingRepository;
    private PnpSendBlockService pnpSendBlockService;
    private PnpFlexTemplateRepository pnpFlexTemplateRepository;

    @Autowired
    public PnpService(PnpMainMitakeRepository pnpMainMitakeRepository,
                      PnpDetailMitakeRepository pnpDetailMitakeRepository, PnpMainEvery8dRepository pnpMainEvery8dRepository,
                      PnpDetailEvery8dRepository pnpDetailEvery8dRepository, PnpMainUnicaRepository pnpMainUnicaRepository,
                      PnpDetailUnicaRepository pnpDetailUnicaRepository, PnpMainMingRepository pnpMainMingRepository,
                      PnpDetailMingRepository pnpDetailMingRepository,
                      PnpFlexTemplateRepository pnpFlexTemplateRepository, PnpSendBlockService pnpSendBlockService
    ) {

        this.pnpMainMitakeRepository = pnpMainMitakeRepository;
        this.pnpDetailMitakeRepository = pnpDetailMitakeRepository;
        this.pnpMainEvery8dRepository = pnpMainEvery8dRepository;
        this.pnpDetailEvery8dRepository = pnpDetailEvery8dRepository;
        this.pnpMainUnicaRepository = pnpMainUnicaRepository;
        this.pnpDetailUnicaRepository = pnpDetailUnicaRepository;
        this.pnpMainMingRepository = pnpMainMingRepository;
        this.pnpDetailMingRepository = pnpDetailMingRepository;
        this.pnpFlexTemplateRepository = pnpFlexTemplateRepository;
        this.pnpSendBlockService = pnpSendBlockService;
    }

    /**
     * Save Detail SendTime
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     */
    public PnpDetail saveDetail(PnpDetail pnpDetail) {
        PnpFtpSourceEnum source = PnpFtpSourceEnum.findEnumByCode(pnpDetail.getSource());
        PnpStatusEnum status = PnpStatusEnum.findEnumByName(pnpDetail.getStatus());
        if (status != null) {
            switch (status) {
                case COMPLETE:
                case BC_SENT_COMPLETE:
                case PNP_SENT_COMPLETE:
                    pnpDetail.setSendTime(Calendar.getInstance().getTime());
                    log.info(String.format("Update SendTime: %s, Status: %s", pnpDetail.getSendTime(), status));
                    break;
                default:
            }
        }
        pnpDetail.setModifyTime(new Date());
        log.info(String.format("Before Save Detail:%n%s", pnpDetail.toString()));

        if (source == null) {
            return null;
        }
        switch (source) {
            case MITAKE:
                return pnpDetailMitakeRepository.save((PnpDetailMitake) pnpDetail);
            case MING:
                return pnpDetailMingRepository.save((PnpDetailMing) pnpDetail);
            case EVERY8D:
                return pnpDetailEvery8dRepository.save((PnpDetailEvery8d) pnpDetail);
            case UNICA:
                return pnpDetailUnicaRepository.save((PnpDetailUnica) pnpDetail);
            default:
                return null;
        }
    }

    /**
     * Update PnpMain and PnpDetail Status
     *
     * @param pnpMain pnpMain
     * @param status  status
     * @see com.bcs.core.taishin.circle.pnp.scheduler.PnpTaskService#startTask
     * 批次排程開始將進入排程的訊息狀態更改為Delay
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void updateMainAndDetailStatus(PnpMain pnpMain, String status) {
        List<PnpDetail> details = pnpMain.getPnpDetails();
        for (PnpDetail detail : details) {
            detail.setStatus(status);
            saveDetail(detail);
        }
        Date now = Calendar.getInstance().getTime();

        PnpFtpSourceEnum source = PnpFtpSourceEnum.findEnumByCode(pnpMain.getSource());
        if (source == null) {
            return;
        }

        switch (source) {
            case MITAKE:
                pnpMainMitakeRepository.updatePnpMainMitakeStatus(status, now, pnpMain.getPnpMainId());
                break;
            case MING:
                pnpMainMingRepository.updatePnpMainMingStatus(status, now, pnpMain.getPnpMainId());
                break;
            case EVERY8D:
                pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(status, now, pnpMain.getPnpMainId());
                break;
            case UNICA:
                pnpMainUnicaRepository.updatePnpMainUnicaStatus(status, now, pnpMain.getPnpMainId());
                break;
            default:
                break;
        }
        log.info("Status is Updated to Delay!!");
    }

    /**
     * BC 訊息推播 Use REST Template Send BC Message To Line
     *
     * @param pnpMain      pnpMain
     * @param sendRef      sendRef
     * @param selfActorRef selfActorRef
     * @see com.bcs.core.taishin.circle.pnp.akka.handler.PnpPushMessageActor#onReceive
     * 排程時間過期立即執行
     * @see com.bcs.core.taishin.circle.pnp.scheduler.PnpTask#execute 排程執行判斷為PNP
     */
    @SuppressWarnings("unchecked")
    public void pushLineMessage(PnpMain pnpMain, ActorRef sendRef, ActorRef selfActorRef) {

        String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
        String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(),
                CONFIG_STR.CHANNEL_TOKEN.toString(), true);
        String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(),
                CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);

        /* 設定 request headers */
        HttpHeaders headers = getLineApiHttpHeaders(accessToken, serviceCode);
        List<PnpDetail> details = pnpMain.getPnpDetails();

        log.info("Process Flow: {}", pnpMain.getProcFlow());

        for (PnpDetail detail : details) {
            PnpProcessFlowEnum processFlow = PnpProcessFlowEnum.findEnumByCode(pnpMain.getProcFlow());
            String nextStage;

            if (processFlow == null) {
                log.warn("Process flow is null, to bc!!");
                processFlow = PnpProcessFlowEnum.BC;
            }
            /* User In Block List */
            if (userInBlackList(detail.getUid(), detail.getPhone())) {
                switch (processFlow) {
                    case BC_SMS:
                    case BC_PNP_SMS:
                        nextStage = "BC_USER_IN_BLACK_LIST_TO_SMS";
                        break;
                    case BC:
                    default:
                        nextStage = "BC_USER_IN_BLACK_LIST";
                        break;
                }
                gotoNext(nextStage, detail, sendRef, selfActorRef, "");
                continue;
            }

            if (LineUser.STATUS_SYS_ADD.equals(detail.getBindStatus())) {
                switch (processFlow) {
                    case BC_SMS:
                        nextStage = "USER_IS_SYSTEM_ADD_IGNORE_TO_SMS";
                        break;
                    case BC_PNP_SMS:
                        nextStage = "USER_IS_SYSTEM_ADD_IGNORE_TO_PNP";
                        break;
                    case BC:
                    default:
                        nextStage = "USER_IS_SYSTEM_ADD_IGNORE";
                        break;
                }
                gotoNext(nextStage, detail, sendRef, selfActorRef, "");
                continue;
            }
            /* User Block Channel */
            if (LineUser.STATUS_BLOCK.equals(detail.getBindStatus())) {
                switch (processFlow) {
                    case BC_SMS:
                    case BC_PNP_SMS:
                        nextStage = "BC_USER_BLOCK_CHANNEL_TO_SMS";
                        break;
                    case BC:
                    default:
                        nextStage = "BC_USER_BLOCK_CHANNEL";
                        break;
                }
                gotoNext(nextStage, detail, sendRef, selfActorRef, "");
                continue;
            }
            /* User Uid Not Found */
            if (StringUtils.isBlank(detail.getUid())) {
                switch (processFlow) {
                    case BC_SMS:
                        nextStage = "BC_UID_NOT_FOUND_TO_SMS";
                        break;
                    case BC_PNP_SMS:
                        nextStage = "BC_UID_NOT_FOUND_TO_PNP";
                        break;
                    case BC:
                    default:
                        nextStage = "BC_UID_NOT_FOUND";
                        break;
                }

                gotoNext(nextStage, detail, sendRef, selfActorRef, "");
                continue;
            }

            /* 發送訊息 */
            final Object[] pushResult = pnpPushMessage(url, headers, detail, detail.getUid());
            final boolean isSuccess = (boolean) pushResult[0];
            final String httpStatusCode = (String) pushResult[1];

            if (!isSuccess) {
                switch (processFlow) {
                    case BC_SMS:
                        nextStage = "BC_FAIL_TO_SMS";
                        break;
                    case BC_PNP_SMS:
                        nextStage = "BC_FAIL_TO_PNP";
                        break;
                    case BC:
                    default:
                        nextStage = "BC_FAIL";
                        break;
                }
                gotoNext(nextStage, detail, sendRef, selfActorRef, httpStatusCode);
                continue;
            }

            nextStage = "BC_SUCCESS";
            gotoNext(nextStage, detail, sendRef, selfActorRef, httpStatusCode);
        }
    }

    public boolean userInBlackList(final String uid, final String phone) {
        log.info("UID: {}, Phone: {}", uid, phone);
        if (StringUtils.isNotBlank(uid)) {
            List<PnpSendBlock> pnpSendBlockList = pnpSendBlockService.findByUid(uid);
            if (CollectionUtils.isNotEmpty(pnpSendBlockList)) {
                return true;
            }
        }
        List<PnpSendBlock> list = pnpSendBlockService.findByPhone(phone);
        return CollectionUtils.isNotEmpty(list);
    }

    public void gotoNext(String nextStage, PnpDetail detail, ActorRef sendRef, ActorRef selfActorRef,
                         String httpStatusCode) {
        log.info("Next Stage :{}, {}", nextStage, httpStatusCode);
        PnpDetail d = route(nextStage, detail, httpStatusCode);
        log.info("detail: {}", DataUtils.toPrettyJsonUseJackson(d));
        saveDetail(d);
    }

    public PnpDetail route(String nextStage, PnpDetail detail, String httpStatusCode) {
        switch (nextStage) {
            case "USER_IS_SYSTEM_ADD_IGNORE":
                log.info("USER_IS_SYSTEM_ADD_IGNORE");
                detail.setProcStage(PnpStageEnum.BC.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.USER_IS_SYSTEM_ADD_IGNORE.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "USER_IS_SYSTEM_ADD_IGNORE_TO_PNP":
                log.info("USER_IS_SYSTEM_ADD_IGNORE_PNP");
                detail.setProcStage(PnpStageEnum.PNP.value);
                detail.setStatus(PnpStatusEnum.PROCESS.value);
                detail.setBcStatus(PnpStatusEnum.USER_IS_SYSTEM_ADD_IGNORE_PNP.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "USER_IS_SYSTEM_ADD_IGNORE_TO_SMS":
                log.info("USER_IS_SYSTEM_ADD_IGNORE_SMS");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.USER_IS_SYSTEM_ADD_IGNORE_SMS.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_USER_IN_BLACK_LIST":
                log.info("BC_USER_IN_BLACK_LIST");
                detail.setProcStage(PnpStageEnum.BC.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_USER_IN_BLACK_LIST.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_USER_IN_BLACK_LIST_TO_SMS":
                log.info("BC_USER_IN_BLACK_LIST_TO_SMS");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_USER_IN_BLACK_LIST_SMS_PROCESS.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_USER_BLOCK_CHANNEL":
                log.info("BC_USER_BLOCK_CHANNEL");
                detail.setProcStage(PnpStageEnum.BC.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_USER_BLOCKED.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_USER_BLOCK_CHANNEL_TO_SMS":
                log.info("BC_USER_BLOCK_CHANNEL_TO_SMS");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_USER_BLOCKED_SMS_PROCESS.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_UID_NOT_FOUND":
                log.info("BC_UID_NOT_FOUND");
                detail.setProcStage(PnpStageEnum.BC.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_UID_NOT_FOUND.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_UID_NOT_FOUND_TO_PNP":
                log.info("BC_UID_NOT_FOUND_TO_PNP");
                detail.setProcStage(PnpStageEnum.PNP.value);
                detail.setStatus(PnpStatusEnum.PROCESS.value);
                detail.setBcStatus(PnpStatusEnum.BC_UID_NOT_FOUND_PNP_PROCESS.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_UID_NOT_FOUND_TO_SMS":
                log.info("BC_UID_NOT_FOUND_TO_SMS");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_UID_NOT_FOUND_SMS_PROCESS.value);
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_FAIL":
                log.info("BC_FAIL");
                detail.setProcStage(PnpStageEnum.BC.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_SENT_FAIL.value);
                detail.setLinePushTime(new Date());
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_FAIL_TO_PNP":
                log.info("BC_FAIL_TO_PNP");
                detail.setProcStage(PnpStageEnum.PNP.value);
                detail.setStatus(PnpStatusEnum.PROCESS.value);
                detail.setBcStatus(PnpStatusEnum.BC_SENT_FAIL_PNP_PROCESS.value);
                detail.setLinePushTime(new Date());
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_FAIL_TO_SMS":
                log.info("BC_FAIL_TO_SMS");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_SENT_FAIL_SMS_PROCESS.value);
                detail.setLinePushTime(new Date());
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "BC_SUCCESS":
                log.info("BC_SUCCESS");
                detail.setProcStage(PnpStageEnum.BC.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setBcStatus(PnpStatusEnum.BC_SENT_COMPLETE.value);
                detail.setLinePushTime(new Date());
                detail.setBcHttpStatusCode(httpStatusCode);
                return detail;
            case "PNP_USER_IN_BLOCK_LIST_TO_SMS":
                log.info("PNP_USER_IN_BLOCK_LIST_TO_SMS");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_USER_IN_BLACK_LIST_SMS_PROCESS.value);
                return detail;
            case "PNP_FAIL":
                log.info("PNP_FAIL");
                detail.setProcStage(PnpStageEnum.SMS.value);
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_SENT_TO_LINE_FAIL_SMS_PROCESS.value);
                detail.setPnpTime(new Date());
                return detail;
            case "PNP_SUCCESS":
                log.info("PNP_SUCCESS");
                Date pnpSendTime = new Date();
                detail.setPnpTime(pnpSendTime);
                int expiredUnit = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME_UNIT, true, false);
                int expired = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME, true, false);
                log.info(String.format("expired: %s, expiredUnit: %s", expired, expiredUnit));

                Calendar calendar = new GregorianCalendar();
                calendar.setTime(pnpSendTime);
                calendar.add(expiredUnit, expired);
                detail.setPnpDeliveryExpireTime(calendar.getTime());
                log.info("Pnp Send Time            : " + DataUtils.formatDateToString(pnpSendTime, "yyyy-MM-dd HH:mm:ss"));
                log.info("Pnp Delivery Expire Time : "
                        + DataUtils.formatDateToString(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
                detail.setStatus(PnpStatusEnum.COMPLETE.value);
                detail.setPnpStatus(PnpStatusEnum.PNP_SENT_CHECK_DELIVERY.value);
                return detail;
            default:
                log.info("this is default switch");
                break;

        }
        return detail;
    }

    /**
     * PNP 訊息推播 Use REST Template Send PNP Message To Line
     *
     * @param pnpMain      pnpMain
     * @param sendRef      sendRef
     * @param selfActorRef selfActorRef
     * @see PnpMessageActor#immediatePushMessage 排程時間過期立即執行
     * @see com.bcs.core.taishin.circle.pnp.scheduler.PnpTask#execute 排程執行判斷為PNP
     */
    @SuppressWarnings("unchecked")
    public void pushPnpMessage(PnpMain pnpMain, ActorRef sendRef, ActorRef selfActorRef) {
        try {
            String url = CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED.toString());
            String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(),
                    CONFIG_STR.CHANNEL_TOKEN.toString(), true);
            String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(),
                    CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
            HttpHeaders headers = getLineApiHttpHeaders(accessToken, serviceCode);

            List<PnpDetail> details = pnpMain.getPnpDetails();

            String source = pnpMain.getSource();
            for (PnpDetail detail : details) {
                String nextStage;
                /* User In Block List */
                if (userInBlackList(detail.getUid(), detail.getPhone())) {
                    nextStage = "PNP_USER_IN_BLOCK_LIST_TO_SMS";
                    gotoNext(nextStage, detail, sendRef, selfActorRef, "");
                    continue;
                }

                String deliveryTag = formatMessageToLineDeliveryTag(source, detail);
                headers.set("X-Line-Delivery-Tag", deliveryTag);
                log.info("X-Line-Delivery-Tag : " + deliveryTag);

                /* 發送訊息 */
                Object[] pushResult = pnpPushMessage(url, headers, detail, detail.getPhoneHash());
                boolean isSuccess = (boolean) pushResult[0];
                String httpStatusCode = (String) pushResult[1];

                detail.setPnpHttpStatusCode(httpStatusCode);

                Date pnpSendTime = new Date();
                detail.setPnpTime(pnpSendTime);
                if (isSuccess) {
                    nextStage = "PNP_SUCCESS";
                } else {
                    nextStage = "PNP_FAIL";
                }
                gotoNext(nextStage, detail, sendRef, selfActorRef, httpStatusCode);
                log.debug(String.format("Process Flow: %s, After Proc Stage: %s, After Status: %s", detail.getProcFlow(),
                        detail.getProcStage(), detail.getStatus()));
//                if (sendRef != null) {
//                    log.debug("Tell SendRef: " + sendRef);
//                    sendRef.tell(detail, selfActorRef);
//                } else {
//                    log.debug("Tell PnpAkkaService to Update Status");
//                    pnpAkkaService.tell(detail);
//                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * 設定 request headers
     *
     * @see this#pushLineMessage(PnpMain, ActorRef, ActorRef) BC Push
     * @see this#pushPnpMessage(PnpMain, ActorRef, ActorRef) PNP Push
     **/
    private HttpHeaders getLineApiHttpHeaders(String accessToken, String serviceCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
        return headers;
    }

    /**
     * PNP 通用發送推播訊息程序
     *
     * @param url     Line API URL
     * @param headers request header
     * @param detail  went push message object
     * @param to      UID Or PhoneNumber
     * @return Object[]{sendSuccessFlag, httpStatusCode, errorMsg};
     * @see this#pushPnpMessage(PnpMain, ActorRef, ActorRef) PNP Push
     */
    private Object[] pnpPushMessage(String url, HttpHeaders headers, PnpDetail detail, String to) {
        boolean sendSuccessFlag;
        String httpStatusCode = "";
        String errorMsg = "";
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
        log.debug("Pnp Push RequestBody : " + DataUtils.toPrettyJsonUseJackson(requestBody.toString()));

        /* 將 headers 跟 body 塞進 HttpEntity 中 */
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);

        log.info("Before execute: mainId: {}, detailId: {}, to: {}", new Object[]{detail.getPnpMainId(), detail.getPnpDetailId(), to});
        boolean isDoRetry;
        int i = 0;
        final int retryCountLimit = DEFAULT_RETRY_COUNT;
        do {
            i++;
            log.info("This is count is {}", i);
            try {
                RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
                restfulUtil.execute();
                httpStatusCode = restfulUtil.getStatusCode();
                sendSuccessFlag = "200".equals(httpStatusCode);
                log.info("RestfulUtil.getStatusCode: " + httpStatusCode);
                isDoRetry = false;
            } catch (HttpClientErrorException ce) {
                log.error("HttpClientErrorException", ce);
                sendSuccessFlag = false;
                httpStatusCode = ce.getStatusCode().toString();
                errorMsg = ce.getMessage();
                isDoRetry = i <= retryCountLimit;
                sleepProcess();
            } catch (HttpServerErrorException se) {
                log.error("HttpServerErrorException Error :", se);
                sendSuccessFlag = false;
                httpStatusCode = se.getStatusCode().toString();
                errorMsg = "BC伺服器錯誤，請洽資訊人員";
                isDoRetry = i <= retryCountLimit;
                sleepProcess();
            } catch (NullPointerException ne) {
                log.error("NullPointException", ne);
                sendSuccessFlag = false;
                httpStatusCode = "500";
                errorMsg = "BC伺服器錯誤，請洽資訊人員!";
                isDoRetry = false;
            } catch (Exception e) {
                log.info("Send fail PnpDetailId:" + detail.getPnpDetailId());
                log.error("Send fail Exception:" + e.getMessage());
                sendSuccessFlag = false;
                httpStatusCode = "500";
                errorMsg = "BC伺服器錯誤，請洽資訊人員";
                isDoRetry = false;
            }
        } while (isDoRetry);
        log.info("sendSuccessFlag: {}, httpStatusCode: {}, errorMsg: {}", new Object[]{sendSuccessFlag, httpStatusCode, errorMsg});
        return new Object[]{sendSuccessFlag, httpStatusCode, errorMsg};
    }

    /**
     * 格式化訊息物件成特殊格式 "RecordType;;Source;;MainId;;DetailID;;PhoneHash"
     *
     * @param source source
     * @param detail detail
     * @return String 格式化字串
     */
    private String formatMessageToLineDeliveryTag(String source, PnpDetail detail) {
        String pnpMessage = String.format("%s;;%s;;%s;;%s;;%s", PnpDeliveryRecord.THIS_TYPE, source,
                detail.getPnpMainId().toString(), detail.getPnpDetailId().toString(), detail.getPhoneHash());

        // 64 <= tag length <= 100，不夠則補空格
        return String.format("%1$-" + 64 + "s", pnpMessage);
    }

    /**
     * 將欲傳遞至Line的訊息格式化成Line格式
     * <p>
     * "messages":[ {"type":"text","text":"Hello, world1"},
     * {"type":"text","text":"Hello, world2"} ]
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
        log.info("isDeclarationEnable : " + isDeclarationEnable);
        log.info("declarationMessage  : " + declarationMessage);
        if (isDeclarationEnable && StringUtils.isNotBlank(declarationMessage)) {
            JSONObject declarationMsgMap = new JSONObject();
            declarationMsgMap.put("type", "text");
            declarationMsgMap.put("text", declarationMessage);
            messageArray.put(declarationMsgMap);
        }
        return messageArray;
    }

    private String combineLineFlexMessage(String msg, String templateId) {
        log.info("Msg : " + msg + "Template Id: " + templateId);
        long id;
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

                .replaceAll("footerLinkUrl",
                        "".equals(footerUrl) ? pnpFlexTemplate.getFooterLinkUrl() : footerUrl.trim());

        StringBuilder sb = new StringBuilder();
        String[] buttonTextArray = pnpFlexTemplate.getButtonText().split(",");
        String[] buttonUrlArray = pnpFlexTemplate.getButtonUrl().split(",");
        String[] buttonColorArray = pnpFlexTemplate.getButtonColor().split(",");

        for (int i = 0; i < buttonTextArray.length; i++) {
            String text = buttonTextArray[i];
            String url = i < buttonUrlArray.length ? buttonUrlArray[i] : "";
            String color = i < buttonColorArray.length ? buttonColorArray[i] : "";
            /* Check button parameter is not empty, Require by Line API!! */
            if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(color) && StringUtils.isNotBlank(text)) {
                sb.append(PnpFlexTemplate.fetchDefaultButtonTemplateJson().replace("bodyButtonText", text.trim())
                        .replace("bodyLinkUrl", url.trim()).replace("bodyButtonColor", color).trim());
            }
        }
        templateJson = templateJson.replace("buttonJsonArea", sb.toString());
        log.debug("final templateJson: " + DataUtils.toPrettyJsonUseJackson(templateJson));
        return templateJson;
    }

    private void sleepProcess() {
        int time = getProcessSleepTime();
        if (time < 0) {
            log.warn("Properties [pnp.api.sent.sleep.time] does not found or value is blank, use default value is 5,000ms");
            time = DEFAULT_SLEEP_TIME;
        }
        log.info("Thread Sleep {}ms", time);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Get Line Point Process Sleep Time
     */
    private Integer getProcessSleepTime() {
        return CoreConfigReader.getInteger("pnp.api.sent.sleep.time", true);
    }
}
