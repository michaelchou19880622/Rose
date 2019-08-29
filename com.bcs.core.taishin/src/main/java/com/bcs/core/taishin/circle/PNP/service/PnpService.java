package com.bcs.core.taishin.circle.PNP.service;

import akka.actor.ActorRef;
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
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainUnicaRepository;
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
    private PnpAkkaService pnpAkkaService;

    /**
     * Save By Source Type
     *
     * @param pnpDetail pnpDetail
     * @return 儲存完成後物件
     * @see com.bcs.core.taishin.circle.PNP.akka.handler.PnpUpdateStatusActor#onReceive
     */
    public PnpDetail save(Object pnpDetail) {
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
     * Save
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#save
     */
    private PnpDetail saveMitakeDetailStatus(PnpDetailMitake pnpDetail) {
        if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
        }
        return pnpDetailMitakeRepository.save(pnpDetail);

    }

    /**
     * Save
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#save
     */
    private PnpDetail saveEvery8dDetailStatus(PnpDetailEvery8d pnpDetail) {
        if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
        }
        return pnpDetailEvery8dRepository.save(pnpDetail);

    }

    /**
     * Save
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#save
     */
    private PnpDetail saveUnicaDetailStatus(PnpDetailUnica pnpDetail) {
        if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
        }
        return pnpDetailUnicaRepository.save(pnpDetail);

    }

    /**
     * Save
     *
     * @param pnpDetail pnpDetail
     * @return Saved Object
     * @see this#save
     */
    private PnpDetail saveMingDetailStatus(PnpDetailMing pnpDetail) {
        if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
            pnpDetail.setSendTime(Calendar.getInstance().getTime());
        }
        return pnpDetailMingRepository.save(pnpDetail);

    }

    /**
     * 檢查訊息狀態並更新狀態為Complete
     *
     * @param mainId 訊息ID
     * @see com.bcs.core.taishin.circle.PNP.akka.handler.PnpUpdateStatusActor#onReceive
     */
    public void updatePnpMainStatusComplete(Long mainId, String source) {
        if (StringUtils.isBlank(source)) {
            logger.error("PnpService save getting source is blank!!! MainID :" + mainId);
        }

        /* 需檢查的狀態清單 */
        List<String> status = new ArrayList<>();
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_SCHEDULED);

        switch (source) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
                updatePnpMainMitakeStatusComplete(mainId, status);
                break;
            case AbstractPnpMainEntity.SOURCE_MING:
                updatePnpMainMingStatusComplete(mainId, status);
                break;
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                updatePnpMainEvery8dStatusComplete(mainId, status);
                break;
            case AbstractPnpMainEntity.SOURCE_UNICA:
                updatePnpMainUnicaStatusComplete(mainId, status);
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
    private void updatePnpMainMitakeStatusComplete(Long mainId, List<String> status) {
        if (pnpDetailMitakeRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            Date now = Calendar.getInstance().getTime();
            pnpMainMitakeRepository.updatePnpMainMitakeStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE, now, mainId);
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
    private void updatePnpMainEvery8dStatusComplete(Long mainId, List<String> status) {
        if (pnpDetailEvery8dRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            Date now = Calendar.getInstance().getTime();
            pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE, now, mainId);
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
    private void updatePnpMainUnicaStatusComplete(Long mainId, List<String> status) {
        if (pnpDetailUnicaRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            Date now = Calendar.getInstance().getTime();
            pnpMainUnicaRepository.updatePnpMainUnicaStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE, now, mainId);
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
    private void updatePnpMainMingStatusComplete(Long mainId, List<String> status) {
        if (pnpDetailMingRepository.countByPnpMainIdAndStatus(mainId, status) == 0) {
            Date now = Calendar.getInstance().getTime();
            pnpMainMingRepository.updatePnpMainMingStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE, now, mainId);
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
            save(detail);
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
                logger.info("detail find no UID trans to PNP!!  " + " mainId :" + detail.getPnpMainId() + " detailId :" + detail.getPnpDetailId());
            } else {
                /* 發送訊息 */
                sendSuccessFlag = pushMessage(url, headers, detail);
            }

            if (sendSuccessFlag) {
                /* 發送成功 */
                /* FIXME PNP 發送時間為送給LINE的時間，不等Line回傳
                 *  Line回傳時間另外紀錄
                 * */
                detail.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE);
            } else {
                /* 發送失敗 */
                /* FIXME 20190822 Record ReturnCode and Create DB column and Report column */
                String processFlow = pnpMain.getProcFlow();
                detail.setLinePushTime(Calendar.getInstance().getTime());
                switch (processFlow) {
                    case AbstractPnpMainEntity.PROC_FLOW_BC:
                        detail.setProcStage(AbstractPnpMainEntity.STAGE_BC);
                        detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_FINISH);
                        break;
                    case AbstractPnpMainEntity.PROC_FLOW_BC_SMS:
                        /* FIXME 20190822 PNP Send Fail or devlevery out of date => Send to SMS Process */
                        detail.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
                        detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
                        break;
                    case AbstractPnpMainEntity.PROC_FLOW_BC_PNP_SMS:
                        detail.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
                        detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
                        break;
                    default:
                        break;
                }
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
     * @see PnpMessageActor#onReceive 排程時間過期立即執行
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpTask#execute 排程執行判斷為PNP
     */
    @SuppressWarnings("unchecked")
    public void pushPnpMessage(PnpMain pnpMain, ActorRef sendRef, ActorRef selfActorRef) {

        String url = CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED.toString());
        String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
                CONFIG_STR.ChannelToken.toString(), true);
        String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(),
                CONFIG_STR.ChannelServiceCode.toString(), true);
        HttpHeaders headers = getLineApiHttpHeaders(accessToken, serviceCode);

        List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();

        String source = pnpMain.getSource();
        logger.info("pushLineMessage pnpMain.getProcFlow():" + pnpMain.getProcFlow());

        boolean sendSuccessFlag;
        for (PnpDetail detail : details) {

            String deliveryTag = formatMessageToLineDeliveryTag(source, detail);
            headers.set("X-Line-Delivery-Tag", deliveryTag);
            logger.debug("X-Line-Delivery-Tag : " + deliveryTag);

            /* 發送訊息 */
            sendSuccessFlag = pushMessage(url, headers, detail);
            detail.setPnpTime(Calendar.getInstance().getTime());
            if (sendSuccessFlag) {
                /* 發送成功 */
                //待web hook在24小時內收到DELIVERY則將該則訊息update成COMPLETE，若24小時內沒收到DELIVERY則將該訊息轉發SMS
                detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_CHECK_DELIVERY);

                //設定PnpDeliveryExpireTime,SMS排程將抓取status = CHECK_DELIVERY 且 now date > PnpDeliveryExpireTime 的資料
                Date now = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(now);

                int expiredUnit = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME_UNIT, true, false);
                int expired = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME, true, false);

                /* 設定PnpDeliveryExpireTime */
                calendar.add(expiredUnit, expired);
                detail.setPnpDeliveryExpireTime(calendar.getTime());
            } else {
                /* 發送失敗 */
                detail.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
                detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
            }
            if (sendRef != null) {
                sendRef.tell(detail, selfActorRef);
            } else {
                pnpAkkaService.tell(detail);
            }
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
     * @see this#pushPnpMessage(PnpMain, ActorRef, ActorRef)  PNP Push
     */
    private boolean pushMessage(String url, HttpHeaders headers, PnpDetail detail) {
        boolean sendSuccessFlag;
        JSONObject requestBody = new JSONObject();

        requestBody.put("to", detail.getPhoneHash());
        requestBody.put("messages", combineLineTextMessage(detail.getMsg()));

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


//	/**
//	 * send email to system admin setting user
//	 * @param title
//	 * @param content
//	 */
//	public void sendMail(String title, String content) {
//		logger.info("Billing Notice Send email : [" + title + "]" +  content);
//		String fromAddress = CoreConfigReader.getString(null, EMAIL_CONFIG.FROM.toString(),true);
//		if (StringUtils.isBlank(fromAddress)) {
//			logger.error("Billing Notice Send email from Address is null ");
//			return;
//		}
//		String host = CoreConfigReader.getString(null, EMAIL_CONFIG.HOST.toString(),true);
//		int port = -1;
//		try{
//			port = Integer.parseInt(CoreConfigReader.getString(null, EMAIL_CONFIG.PORT.toString(),true));
//		}
//		catch(Exception e){
//			logger.error("Billing Notice Send email smtp port is not number ");
//		}
//		final String username = CoreConfigReader.getString(null, EMAIL_CONFIG.USERNAME.toString(),true);
//		final String password = CoreConfigReader.getString(null, EMAIL_CONFIG.PASSWORD.toString(),true);// your password
//		String auth = CoreConfigReader.getString(null, EMAIL_CONFIG.AUTH.toString(),true);
//		String starttls = CoreConfigReader.getString(null, EMAIL_CONFIG.STARTTLS_ENABLE.toString(),true);
//		String debug = CoreConfigReader.getString(null, EMAIL_CONFIG.DEBUG.toString(),true);
//		Properties props = new Properties();
//		props.put("mail.smtp.host", host);
//		props.put("mail.smtp.auth", StringUtils.isBlank(auth) ? "false" : auth);
//		props.put("mail.smtp.starttls.enable", StringUtils.isBlank(starttls) ? "false" : starttls);
//		props.put("mail.smtp.port", port);
//		props.put("mail.debug", StringUtils.isBlank(debug) ? "false" : debug);
//		if (StringUtils.isBlank(host) || port == -1 || StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
//			logger.error("Billing Notice Send email setting wrong ");
//			return;
//		}
//		String adminMail = CoreConfigReader.getString(null, EMAIL_CONFIG.TO_ADMIN.toString(),true);
//		if (StringUtils.isBlank(adminMail)) {
//			logger.error("Billing Notice Send email Recipients is empty ");
//			return;
//		}
//		String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
//		String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
//
//
//		try {
//			final String decodePassword = CryptUtil.Decrypt(CryptUtil.AES, password, secret, iv);
//			Session session = Session.getInstance(props, new Authenticator() {
//				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
//					return new javax.mail.PasswordAuthentication(username, decodePassword);
//				}
//			});
//			List<Address> recipients = new ArrayList<>();
//			String[] toArray = adminMail.split(",");
//			for (final String address : toArray) {
//				if (StringUtils.isNotBlank(address)) {
//					recipients.add(new InternetAddress(address.trim()));
//				}
//			}
//
//			Message message = new MimeMessage(session);
//			message.setFrom(new InternetAddress(fromAddress));
//			message.setRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[0]));
//			message.setSubject(title);
//			message.setText(content);
//
//			Transport transport = session.getTransport("smtp");
//			transport.connect(host, port, username, password);
//
//			Transport.send(message);
//
//		} catch (MessagingException e) {
//			logger.error("Billing Notice Send email error : " + e.getMessage());
//		} catch (Exception e1) {
//			logger.error("Billing Notice Send email error : " + e1.getMessage());
//		}
//	}
}
