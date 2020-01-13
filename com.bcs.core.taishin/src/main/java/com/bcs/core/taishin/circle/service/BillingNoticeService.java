package com.bcs.core.taishin.circle.service;

import akka.actor.ActorRef;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.EMAIL_CONFIG;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgActionRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeDetailRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeMainRepository;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.RestfulUtil;
import lombok.extern.slf4j.Slf4j;
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

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class BillingNoticeService {
    @Autowired
    private BillingNoticeMainRepository billingNoticeMainRepository;
    @Autowired
    private BillingNoticeDetailRepository billingNoticeDetailRepository;
    @Autowired
    private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
    @Autowired
    private BillingNoticeContentTemplateMsgActionRepository billingNoticeContentTemplateMsgActionRepository;
    @Autowired
    private BillingNoticeAkkaService billingNoticeAkkaService;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     * 找尋開啟的templateId
     *
     * @return
     */
    public List<String> findProductSwitchOnTemplateId() {
        return billingNoticeContentTemplateMsgRepository.findProductSwitchOnTemplateId();
    }

    /**
     * 是否宵禁中
     *
     * @param template
     * @return
     */
    public boolean isCurfew(BillingNoticeContentTemplateMsg template, Calendar now) {
        boolean iscurfew = false;
        try {
            if (StringUtils.isNotBlank(template.getCurfewEndTime()) && StringUtils.isNotBlank(template.getCurfewStartTime())) {
                Calendar curfewStartTime = (Calendar) now.clone();
                String[] startTime = template.getCurfewStartTime().split(":");
                curfewStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
                curfewStartTime.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
                curfewStartTime.set(Calendar.SECOND, Integer.parseInt(startTime[2]));
                curfewStartTime.set(Calendar.MILLISECOND, 0);
                Calendar curfewEndTime = (Calendar) now.clone();
                String[] endTime = template.getCurfewEndTime().split(":");
                curfewEndTime.set(Calendar.MILLISECOND, 0);
                curfewEndTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
                curfewEndTime.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
                curfewEndTime.set(Calendar.SECOND, Integer.parseInt(endTime[2]));
                if (curfewStartTime.before(curfewEndTime)) {
                    //未跨日 EX: 09:00:00 ~ 13:00:00
                } else {
                    //跨日 EX: 3/19 23:00:00 ~ 3/20 01:00:00
                    if (now.after(curfewStartTime)) {
                        // 目前日期等於起始日 EX: now = 3/19 23:02:00
                        curfewEndTime.add(Calendar.DATE, 1);
                    } else {
                        // 目前日期等於截止日 EX: now = 3/20 00:02:00
                        curfewStartTime.add(Calendar.DATE, -1);
                    }
                }

                if (now.getTime().before(curfewEndTime.getTime()) && now.getTime().after(curfewStartTime.getTime())) {
                    // 宵禁中
                    iscurfew = true;
                }
                log.info("iscurfew: " + curfewStartTime.getTime() + "~" + curfewEndTime.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("CurfewEndTime/CurfewStartTime Parse Exception :" + e.getMessage());
        }
        return iscurfew;
    }

    public BillingNoticeDetail save(BillingNoticeDetail billingNoticeDetail) {
        if (billingNoticeDetail.getStatus().equals(BillingNoticeMain.NOTICE_STATUS_COMPLETE)) {
            billingNoticeDetail.setSendTime(Calendar.getInstance().getTime());
        }
        return billingNoticeDetailRepository.save(billingNoticeDetail);
    }

    /**
     * update status 為不推送 並寄信
     *
     * @param billingNoticeMain
     * @return
     */
    public void updateStatusFailAndEmail(BillingNoticeMain billingNoticeMain) {
        updateMainAndDetailStatus(billingNoticeMain, BillingNoticeMain.NOTICE_STATUS_FAIL);
        //  send email Title , mailTemplate
        StringBuilder contextBuilder = new StringBuilder("帳務系統重試失敗 ");
        contextBuilder.append("\n 失敗ID:" + billingNoticeMain.getNoticeMainId());
        if (billingNoticeMain.getExpiryTime() != null) {
            contextBuilder.append("\n EXPIRY_TIME:" + df.format(billingNoticeMain.getExpiryTime()));
        }
        sendMail("帳務系統重試失敗", contextBuilder.toString());
    }

    /**
     * update billingNoticeMain and BillingNoticeDetail status
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void updateMainAndDetailStatus(BillingNoticeMain billingNoticeMain, String status) {
        List<BillingNoticeDetail> details = billingNoticeMain.getDetails();
        for (BillingNoticeDetail detail : details) {
            detail.setStatus(status);
            save(detail);
        }
        Date now = new Date();
        billingNoticeMainRepository.updateBillingNoticeMainStatus(status, now, billingNoticeMain.getNoticeMainId());
    }

    /**
     * send email to system admin setting user
     *
     * @param title
     * @param content
     */
    public void sendMail(String title, String content) {
        log.info("Billing Notice Send email : [" + title + "]" + content);
        String fromAddress = CoreConfigReader.getString(null, EMAIL_CONFIG.FROM.toString(), true, false);
        if (StringUtils.isBlank(fromAddress)) {
            log.error("Billing Notice Send email from Address is null ");
            return;
        }
        String host = CoreConfigReader.getString(null, EMAIL_CONFIG.HOST.toString(), true, false);
        int port = -1;
        try {
            port = Integer.parseInt(CoreConfigReader.getString(null, EMAIL_CONFIG.PORT.toString(), true, false));
        } catch (Exception e) {
            log.error("Billing Notice Send email smtp port is not number ");
        }
        final String username = CoreConfigReader.getString(null, EMAIL_CONFIG.USERNAME.toString(), true, false);
        final String password = CoreConfigReader.getString(null, EMAIL_CONFIG.PASSWORD.toString(), true, false);// your password
        String auth = CoreConfigReader.getString(null, EMAIL_CONFIG.AUTH.toString(), true, false);
        String starttls = CoreConfigReader.getString(null, EMAIL_CONFIG.STARTTLS_ENABLE.toString(), true, false);
        String debug = CoreConfigReader.getString(null, EMAIL_CONFIG.DEBUG.toString(), true, false);
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", StringUtils.isBlank(auth) ? "false" : auth);
        props.put("mail.smtp.starttls.enable", StringUtils.isBlank(starttls) ? "false" : starttls);
        props.put("mail.smtp.port", port);
        props.put("mail.debug", StringUtils.isBlank(debug) ? "false" : debug);
        if (StringUtils.isBlank(host) || port == -1 || StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            log.error("Billing Notice Send email setting wrong ");
            return;
        }
        String adminMail = CoreConfigReader.getString(null, EMAIL_CONFIG.TO_ADMIN.toString(), true, false);
        if (StringUtils.isBlank(adminMail)) {
            log.error("Billing Notice Send email Recipients is empty ");
            return;
        }
        String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true, false);
        String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true, false);


        try {
            final String decodePassword = CryptUtil.Decrypt(CryptUtil.AES, password, secret, iv);
            Session session = Session.getInstance(props, new Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(username, decodePassword);
                }
            });
            List<Address> recipients = new ArrayList<>();
            String[] toArray = adminMail.split(",");
            for (final String address : toArray) {
                if (StringUtils.isNotBlank(address)) {
                    recipients.add(new InternetAddress(address.trim()));
                }
            }

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[0]));
            message.setSubject(title);
            message.setText(content);

            Transport transport = session.getTransport("smtp");
            transport.connect(host, port, username, password);

            Transport.send(message);

        } catch (MessagingException e) {
            log.error("Billing Notice Send email error : " + e.getMessage());
        } catch (Exception e1) {
            log.error("Billing Notice Send email error : " + e1.getMessage());
        }
    }

    /**
     * 若明細已無重試或等待發送/排程中或者傳遞中的狀態資料，則更新主檔狀態為完成
     */
    public void updateBillingNoticeMainStatusComplete(Long mainId) {
        List<String> status = new ArrayList<>();
        status.add(BillingNoticeMain.NOTICE_STATUS_WAIT);
        status.add(BillingNoticeMain.NOTICE_STATUS_RETRY);
        status.add(BillingNoticeMain.NOTICE_STATUS_SENDING);
        status.add(BillingNoticeMain.NOTICE_STATUS_DRAFT);
        status.add(BillingNoticeMain.NOTICE_STATUS_SCHEDULED);
        if (billingNoticeDetailRepository.countByNoticeMainIdAndStatus(mainId, status) == 0) {
            billingNoticeMainRepository.updateBillingNoticeMainStatus(BillingNoticeMain.NOTICE_STATUS_COMPLETE, new Date(), mainId);
        }
    }

    public void pushLineMessage(BillingNoticeMain billingNoticeMain, ActorRef sendRef, ActorRef selfActorRef) {
        log.info("Push Line Message!!");
        String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
        String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
        String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(), CONFIG_STR.ChannelServiceCode.toString(), true);

        /* Headers */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);

        BillingNoticeContentTemplateMsg templateMsg = billingNoticeMain.getTemplate();

        if (templateMsg == null) {
            log.error("Template Error: BillingNoticeContentTemplateMsg or BillingNoticeContentTemplateMsgAction is empty");
            return;
        }

        for (BillingNoticeDetail detail : billingNoticeMain.getDetails()) {
            log.info("To Line Uid: {}", detail.getUid());

            JSONObject requestBody = new JSONObject();
            requestBody.put("to", detail.getUid());
            requestBody.put("messages", combineLineMessage(templateMsg, detail));
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString(), headers);

            try {
                RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
                JSONObject result = restfulUtil.execute();
                log.info("Result: {}", result.toString());
                detail.setStatus(BillingNoticeMain.NOTICE_STATUS_COMPLETE);
                log.info("Execute Success!!");
            } catch (KeyManagementException | NoSuchAlgorithmException e1) {
                log.info("NOTICE_STATUS_RETRY NoticeDetailId:" + detail.getNoticeDetailId());
                log.error("NOTICE_STATUS_RETRY KeyManagementException | NoSuchAlgorithmException: ", e1);
                detail.setStatus(BillingNoticeMain.NOTICE_STATUS_RETRY);
            } catch (HttpClientErrorException he) {
                log.error("HttpClientErrorException:", he);
                JSONObject errorMessage = new JSONObject(he.getResponseBodyAsString());
                if (errorMessage.has("message")) {
                    log.error("HttpClientErrorException StatusCode: {}", he.getStatusCode().toString());
                    if (errorMessage.has("details")) {
                        log.error("HttpClientErrorException Details: {}", errorMessage.getJSONArray("details").toString());
                    }
                }
            } catch (HttpServerErrorException se) {
                log.error("HttpServerErrorException Error :", se);
                JSONObject errorMessage = new JSONObject(se.getResponseBodyAsString());
                if (errorMessage.has("message")) {
                    log.error("HttpServerErrorException StatusCode: {}", se.getStatusCode().toString());
                    if (errorMessage.has("details")) {
                        log.error("HttpServerErrorException Details: {}", errorMessage.getJSONArray("details").toString());
                    }
                }
                detail.setStatus(BillingNoticeMain.NOTICE_STATUS_RETRY);
            } catch (Exception e) {
                log.info("NOTICE_STATUS_RETRY NoticeDetailId:" + detail.getNoticeDetailId());
                log.error("NOTICE_STATUS_RETRY Exception:", e);
                detail.setStatus(BillingNoticeMain.NOTICE_STATUS_RETRY);
            }

            if (sendRef != null) {
                sendRef.tell(detail, selfActorRef);
            } else {
                billingNoticeAkkaService.tell(detail);
            }

        }

    }

    /**
     * 組合line message
     */
    private JSONArray combineLineMessage(BillingNoticeContentTemplateMsg templateMsg, BillingNoticeDetail detail) {
        JSONObject templateObject;
        switch (templateMsg.getTemplateType()) {
            case BillingNoticeContentTemplateMsg.TEMPLATE_TYPE_BUTTONS:
                templateObject = typeButtonProcess(templateMsg, detail);
                break;
            case BillingNoticeContentTemplateMsg.TEMPLATE_TYPE_CAROUSEL:
                templateObject = carouselProcess(templateMsg, detail);
                break;
            default:
                templateObject = new JSONObject();
                break;
        }

        JSONObject message = new JSONObject();
        message.put("type", detail.getMsgType());
        message.put("altText", templateMsg.getAltText());
        message.put("template", templateObject);

        JSONArray messagesArray = new JSONArray();
        messagesArray.put(message);

        log.info("Message Array: {}", DataUtils.toPrettyJsonUseJackson(messagesArray));
        return messagesArray;
    }

    private JSONObject typeButtonProcess(BillingNoticeContentTemplateMsg templateMsg, BillingNoticeDetail detail) {
        String imageUrl = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, templateMsg.getTemplateImageId());
        log.info("imageUrl1: " + imageUrl);
        JSONObject templateObject = new JSONObject();
        templateObject.put("type", templateMsg.getTemplateType());
        templateObject.put("thumbnailImageUrl", imageUrl);
        templateObject.put("title", detail.getTitle());
        templateObject.put("text", detail.getText());
        templateObject.put("actions", getActionArray(templateMsg, detail));
        return templateObject;
    }

    private JSONArray getActionArray(BillingNoticeContentTemplateMsg templateMsg, BillingNoticeDetail detail) {
        JSONArray actionsArray = new JSONArray();
        List<BillingNoticeContentTemplateMsgAction> actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(templateMsg.getTemplateId());
        for (BillingNoticeContentTemplateMsgAction action : actions) {
            BillingNoticeContentTemplateMsgActionType type = BillingNoticeContentTemplateMsgActionType.findActionType(action.getActionType());
            if (type == null) {
                log.error("Template Error : BillingNoticeContentTemplateMsgActionType is not support . Type:" + action.getActionType());
                continue;
            }
            JSONObject actionObject = type.getJSONObject(action, detail.getUid());
            actionsArray.put(actionObject);
        }
        return actionsArray;
    }

    private JSONObject carouselProcess(BillingNoticeContentTemplateMsg templateMsg, BillingNoticeDetail detail) {
        JSONObject templateObject = new JSONObject();
        String imageUrl;
        JSONArray actionsArray;
        List<BillingNoticeContentTemplateMsgAction> actions;// columns
        JSONArray columnsArray = new JSONArray();

        // main column
        JSONObject columnObject = new JSONObject();
        imageUrl = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, templateMsg.getTemplateImageId());
        //imageUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, templateMsg.getTemplateImageId());
        //imageUrl = "https://images.unsplash.com/photo-1556228720-9b1e04f13f63";
        log.info("imageUrl1: " + imageUrl);
        columnObject.put("thumbnailImageUrl", imageUrl);
        columnObject.put("title", detail.getTitle());
        columnObject.put("text", detail.getText());
        // action
        actionsArray = new JSONArray();
        actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(templateMsg.getTemplateId());
        for (BillingNoticeContentTemplateMsgAction action : actions) {
            BillingNoticeContentTemplateMsgActionType type = BillingNoticeContentTemplateMsgActionType.findActionType(action.getActionType());
            if (type != null) {
                JSONObject actionObject = type.getJSONObject(action, detail.getUid());
                actionsArray.put(actionObject);
            } else {
                log.error("Template Error : BillingNoticeContentTemplateMsgActionType is not support . Type:" + action.getActionType());
            }
        }
        columnObject.put("actions", actionsArray);
        columnsArray.put(columnObject);

        // child columns
        List<BillingNoticeContentTemplateMsg> childs = billingNoticeContentTemplateMsgRepository.findByParentTemplateId(templateMsg.getTemplateId());
        log.info("childs:" + childs);
        for (BillingNoticeContentTemplateMsg child : childs) {
            // column
            columnObject = new JSONObject();
            imageUrl = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, child.getTemplateImageId());
            //imageUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, child.getTemplateImageId());
            //imageUrl = "https://images.unsplash.com/photo-1556228720-9b1e04f13f63";
            //log.info("imageUrl1: " + imageUrl);
            columnObject.put("thumbnailImageUrl", imageUrl);
            columnObject.put("title", child.getTemplateTitle());
            columnObject.put("text", child.getTemplateText());
            // action
            actionsArray = new JSONArray();
            actions = billingNoticeContentTemplateMsgActionRepository.findNotDeletedTemplateId(child.getTemplateId());
            for (BillingNoticeContentTemplateMsgAction action : actions) {
                BillingNoticeContentTemplateMsgActionType type = BillingNoticeContentTemplateMsgActionType.findActionType(action.getActionType());
                if (type != null) {
                    JSONObject actionObject = type.getJSONObject(action, detail.getUid());
                    actionsArray.put(actionObject);
                } else {
                    log.error("Template Error : BillingNoticeContentTemplateMsgActionType is not support . Type:" + action.getActionType());
                }
            }
            columnObject.put("actions", actionsArray);
            columnsArray.put(columnObject);
        }

        // template
        templateObject.put("type", templateMsg.getTemplateType()); //carousel
        templateObject.put("columns", columnsArray);
        return templateObject;
    }

}
