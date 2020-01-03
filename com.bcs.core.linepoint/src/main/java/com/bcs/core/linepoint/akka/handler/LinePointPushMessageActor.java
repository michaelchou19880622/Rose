package com.bcs.core.linepoint.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.bot.scheduler.handler.ExecuteSendMsgTask;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.RestfulUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ???
 */
@Slf4j
public class LinePointPushMessageActor extends UntypedActor {

//	final static int TARGET_INDEX_OF_SLEEP = 10;
    private static final int DEFAULT_SLEEP_TIME = 50;

    /**
     * @see LinePointPushMasterActor#onReceive
     */
    @Override
    public void onReceive(Object object) throws Exception {
        if (object instanceof LinePointPushModel) {
            final String url = getUrl();
            LinePointMainService linePointMainService = ApplicationContextProvider.getApplicationContext().getBean(LinePointMainService.class);
            LinePointPushModel pushApiModel = (LinePointPushModel) object;
            LinePointMain linePointMain = linePointMainService.findOne(pushApiModel.getEventId());
            /* Header */
            HttpHeaders headers = getHttpHeaders();
            JSONObject requestBody = getRequestBody();
            JSONArray detailIds = pushApiModel.getDetailIds();

            for (Object obj : detailIds) {
                mainProcess(url, linePointMainService, pushApiModel, linePointMain, headers, requestBody, obj);
            }
        }
    }

    private void mainProcess(String url, LinePointMainService linePointMainService, LinePointPushModel pushApiModel, LinePointMain linePointMain, HttpHeaders headers, JSONObject requestBody, Object obj) throws Exception {
        LinePointDetailService linePointDetailService = ApplicationContextProvider.getApplicationContext().getBean(LinePointDetailService.class);
        final String detailIdStr = String.format("%s", obj.toString());
        final Long detailId = Long.parseLong(detailIdStr);
        log.info("detailId: {}", detailId);

        final Long applicationTime = System.currentTimeMillis();
        LinePointDetail detail = linePointDetailService.findOne(detailId);
        detail.setTriggerTime(pushApiModel.getTriggerTime());
        detail.setDetailType(LinePointDetail.DETAIL_TYPE_ISSUE_BCS);
        final String orderKey = getOrderKey(pushApiModel, detail);
        requestBody.put("amount", detail.getAmount());
        requestBody.put("memberId", detail.getUid());
        requestBody.put("orderKey", orderKey);
        requestBody.put("applicationTime", applicationTime);
        sendProcess(url, linePointMain, headers, requestBody, detail);

        detail.setOrderKey(orderKey);
        detail.setApplicationTime(applicationTime);
        detail.setSendTime(new Date());

        log.info("detail1: {}" + detail.toString());
        linePointDetailService.save(detail);
        linePointMainService.save(linePointMain);

        if (LinePointDetail.STATUS_SUCCESS.equals(detail.getStatus())) {
            successProcess(linePointMain, detail);
        }
    }

    private void sendProcess(String url, LinePointMain linePointMain, HttpHeaders headers, JSONObject requestBody, LinePointDetail detail) throws InterruptedException {
        int retryCountLimit = 1;
        boolean isDoRetry;
        do {
            int i = 0;
            try {
                i++;
                log.info("This Count is {}", i);
                RestfulUtil restfulUtil = new RestfulUtil(
                        HttpMethod.POST,
                        url,
                        new HttpEntity<>(requestBody.toString(), headers)
                );

                final JSONObject responseObject = restfulUtil.execute();
                log.info("RO1: {}", responseObject.toString());

                final String id = responseObject.getString("transactionId");
                final Long time = responseObject.getLong("transactionTime");
                final String type = responseObject.getString("transactionType");
                final Integer amount = responseObject.getInt("transactionAmount");
                final Integer balance = responseObject.getInt("balance");

                setDetail(detail, id, time, type, amount, balance);

                linePointMain.setSuccessfulAmount(linePointMain.getSuccessfulAmount() + amount);
                linePointMain.setSuccessfulCount(linePointMain.getSuccessfulCount() + 1);
                isDoRetry = false;
            } catch (HttpClientErrorException e) {
                log.info("HttpClientErrorException", e);
                detail.setMessage(e.getResponseBodyAsString().substring(0, 200));
                detail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointMain.setFailedCount(linePointMain.getFailedCount() + 1);
                sleepProcess();
                isDoRetry = i <= retryCountLimit;
            } catch (HttpServerErrorException e) {
                log.info("HttpServerErrorException", e);
                detail.setMessage(e.getStatusText().substring(0, 200));
                detail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointMain.setFailedCount(linePointMain.getFailedCount() + 1);
                sleepProcess();
                isDoRetry = i <= retryCountLimit;
            } catch (Exception e) {
                log.info("Exception", e);
                detail.setMessage(e.getMessage().substring(0, 200));
                detail.setStatus(LinePointDetail.STATUS_FAIL);
                linePointMain.setFailedCount(linePointMain.getFailedCount() + 1);
                sleepProcess();
                isDoRetry = i <= retryCountLimit;
            }
        } while (isDoRetry);
    }

    private void sleepProcess() throws InterruptedException {
        int time = getLinePointProcessSleepTime();
        if (time < 0) {
            time = DEFAULT_SLEEP_TIME;
        }
        log.info("Thread Sleep {}ms", time);
        Thread.sleep(time);

//				if (i % TARGET_INDEX_OF_SLEEP == 0) {
//					log.info(String.format("%d/%d", ((i % TARGET_INDEX_OF_SLEEP == 0) ? TARGET_INDEX_OF_SLEEP : (i % TARGET_INDEX_OF_SLEEP)), TARGET_INDEX_OF_SLEEP));
//					log.info("Try to sleep 5000 milliseconds...");
//
//					Thread.sleep(5000);
//				}
    }

    private void successProcess(LinePointMain linePointMain, LinePointDetail detail) throws Exception {
        MsgMainService msgMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgMainService.class);
        MsgSendMainService msgSendMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class);
        MsgDetailService msgDetailService = ApplicationContextProvider.getApplicationContext().getBean(MsgDetailService.class);
        Long msgId = linePointMain.getAppendMessageId();
        if (msgId != null) {
            log.info("LinePoint發送成功 開始發訊息給會員");
            MsgMain msgMain = msgMainService.findOne(msgId);
            log.info("ExecuteSendMsg: msgMain: {}", msgMain);
            List<String> midList = new ArrayList<>();
            midList.add(detail.getUid());

            if (msgMain != null) {
                MsgSendMain msgSendMain = msgSendMainService.copyFromMsgMain(msgId, (long) midList.size(), "LPSG;" + linePointMain.getTitle());
                List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);
                ExecuteSendMsgTask runTask = new ExecuteSendMsgTask();
                runTask.sendMsgToMids(midList, details, msgSendMain.getMsgSendId());
                log.info("LinePoint訊息發出 ");
            }
        }
    }


    private void setDetail(LinePointDetail detail, String id, Long time, String type, Integer amount, Integer balance) {
        detail.setTranscationId(id);
        detail.setTranscationTime(time);
        detail.setTranscationType(type);
        detail.setTransactionAmount(amount);
        detail.setBalance(balance);
        detail.setMessage("");
        detail.setStatus(LinePointDetail.STATUS_SUCCESS);
    }

    private String getOrderKey(LinePointPushModel pushApiModel, LinePointDetail detail) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256");
        String hashStr = String.format("%s%d%d", detail.getUid(), System.currentTimeMillis(), pushApiModel.getEventId());
        String hash = DigestUtils.md5Hex(hashStr);
        salt.update(hash.getBytes(StandardCharsets.UTF_8.name()));
        return bytesToHex(salt.digest()).substring(0, 48);
    }

    private JSONObject getRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("clientId", getClientId());
        return requestBody;
    }

    /**
     * getUrl: https://api.line.me/pointConnect/v1/issue
     */
    private String getUrl() {
        return CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_PUSH_URL.toString(), true);
    }

    /**
     * getClientId: 10052
     */
    private String getClientId() {
        return CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true);
    }

    /**
     * GetAccessToken
     */
    private String getAccessToken() {
        return CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
    }

    /**
     * Get Line Point Process Sleep Time
     */
    private Integer getLinePointProcessSleepTime() {
        return CoreConfigReader.getInteger("line.point.api.sent.sleep.time", true);
    }

    /**
     * initialize request header
     * TaiShin Bank.ChannelToken
     */
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken());
        return headers;
    }


    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}