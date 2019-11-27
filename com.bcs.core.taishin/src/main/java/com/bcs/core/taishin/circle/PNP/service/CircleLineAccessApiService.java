package com.bcs.core.taishin.circle.PNP.service;

import com.bcs.core.api.service.LineTokenApiService;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.ObjectUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.response.BotApiResponse;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class CircleLineAccessApiService {
    public static final String LINE_API_SYNC = "LINE_API_SYNC";

    private Timer flushTimer = new Timer();

    private Timer checkTokenTimer = new Timer();

    private class CustomTask extends TimerTask {

        @Override
        public void run() {

            try {
                // Check Data Sync
                Boolean isReSyncData = DataSyncUtil.isReSyncData(LINE_API_SYNC);
                if (isReSyncData) {
                    lineMessagingServiceMap.clear();
                    HttpClientUtil.clearData();
                    DataSyncUtil.syncDataFinish(LINE_API_SYNC);
                }
            } catch (Throwable e) {
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }

    public CircleLineAccessApiService() {

        flushTimer.schedule(new CustomTask(), 120000, 30000);
        checkTokenTimer.schedule(new CustomTaskCheckToken(), 150_000, 43_200_000);
    }

    private class CustomTaskCheckToken extends TimerTask {

        @Override
        public void run() {

            try {
                List<Object[]> channels = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%.ChannelId");
                if (channels != null && channels.size() > 0) {
                    for (Object[] channel : channels) {
                        String configId = (String) channel[0];
                        logger.info("callVerifyAPIAndIssueToken configId:" + configId);

                        if (StringUtils.isNotBlank(configId) && configId.indexOf(".") > 0) {
                            String[] split = configId.split("\\.");
                            if (split != null && split.length == 2) {
                                String channelId = split[0];
                                ObjectNode node = CircleLineAccessApiService.callVerifyAPIAndIssueToken(channelId, true);
                                logger.info("callVerifyAPIAndIssueToken:" + channelId + ", isReIssue: " + node.get("isReIssue"));
                                logger.info("callVerifyAPIAndIssueToken:" + ObjectUtil.objectToJsonStr(node));
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }

    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] LineAccessApiService cleaning up...");

        flushTimer.cancel();
        checkTokenTimer.cancel();
        logger.info("[DESTROY] LineAccessApiService destroyed.");
    }

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(CircleLineAccessApiService.class);

    private static Map<String, List<LineMessagingService>> lineMessagingServiceMap = new HashMap<String, List<LineMessagingService>>();

    private static LineMessagingService getService(String ChannelId) {
        List<LineMessagingService> lineMessagingServices = lineMessagingServiceMap.get(ChannelId);
        if (lineMessagingServices == null || lineMessagingServices.size() == 0) {

            String channelToken = CoreConfigReader.getString(ChannelId, CONFIG_STR.ChannelToken.toString(), true);

            if (lineMessagingServices == null) {
                lineMessagingServices = new ArrayList<LineMessagingService>();
                lineMessagingServiceMap.put(ChannelId, lineMessagingServices);
            }

            if (lineMessagingServices.size() == 0) {
                for (int i = 0; i < 300; i++) {

                    LineMessagingServiceBuilder builder = LineMessagingServiceBuilder.create(channelToken);
                    builder.connectTimeout(300_000);
                    builder.readTimeout(300_000);
                    builder.writeTimeout(300_000);
                    LineMessagingService lineMessagingService = builder.build();

                    LineMessagingServiceBuilderBcs bcs = new LineMessagingServiceBuilderBcs();
                    try {
                        String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);
                        if (StringUtils.isNotBlank(proxyUrl)) {
                            lineMessagingService = bcs.build(builder, true, proxyUrl);
                        }
                    } catch (Exception e) {
                        logger.error(ErrorRecord.recordError(e));
                    }
                    lineMessagingServices.add(lineMessagingService);
                }
            }
        }

        return randomOne(lineMessagingServices);
    }

    private static LineMessagingService randomOne(List<LineMessagingService> lineMessagingServices) {
        logger.debug("randomOne Size:" + lineMessagingServices.size());

        int index = new Random().nextInt(lineMessagingServices.size());
        return lineMessagingServices.get(index);
    }

    public static void clearData() {
        for (List<LineMessagingService> list : lineMessagingServiceMap.values()) {
            list.clear();
        }
        lineMessagingServiceMap.clear();
        HttpClientUtil.clearData();
        DataSyncUtil.settingReSync(LINE_API_SYNC);
    }

    public static Response<BotApiResponse> sendToLine(SendToBotModel sendToBotModel) throws Exception {
        logger.debug("sendToLine:" + sendToBotModel);

        if (SEND_TYPE.REPLY_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getReplyMessage());
            try {

                Response<BotApiResponse> response = getService(sendToBotModel.getChannelId())
                        .replyMessage(sendToBotModel.getReplyMessage())
                        .execute();
                logger.debug(response.code());

                status = response.code();

                if (401 == status) {
                    callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
                    clearData();
                }

                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, start, status, postMsg, status + "");
                return response;
            } catch (Exception e) {
                String error = ErrorRecord.recordError(e, false);
                logger.error(error);
                SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, error, e.getMessage());
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi_Error, start, status, postMsg, status + "");
                throw e;
            }
        } else if (SEND_TYPE.PUSH_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getPushMessage());
            try {

                Response<BotApiResponse> response = getService(sendToBotModel.getChannelId())
                        .pushMessage(sendToBotModel.getPushMessage())
                        .execute();
                logger.debug(response.code());

                status = response.code();

                if (401 == status) {
                    callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
                    clearData();
                }

                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, start, status, postMsg, status + "");
                return response;
            } catch (Exception e) {
                String error = ErrorRecord.recordError(e, false);
                logger.error(error);
                SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, error, e.getMessage());
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi_Error, start, status, postMsg, status + "");
                throw e;
            }
        }

        return null;
    }

    public static Response<ResponseBody> getImageFromLine(String channelId, String msgId) throws Exception {
        logger.debug("getImageFromLine:" + msgId);

        Date start = new Date();
        int status = 0;

        try {

            Response<ResponseBody> response = getService(channelId)
                    .getMessageContent(msgId)
                    .execute();
            logger.debug(response.code());

            status = response.code();

            if (401 == status) {
                callVerifyAPIAndIssueToken(channelId, true);
                clearData();
            }

            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi, start, status, msgId, status + "");
            return response;
        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            logger.error(error);
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi, error, e.getMessage());
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi_Error, start, status, msgId, status + "");
            throw e;
        }
    }

    public static ObjectNode callVerifyAPIAndIssueToken(String channelId, boolean reIssue) throws Exception {
        String access_token = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);

        LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
        ObjectNode callVerifyResult = lineTokenApiService.callVerifyAPI(access_token);
        logger.info("callVerifyResult:" + callVerifyResult);

        JsonNode expires_in = callVerifyResult.get("expires_in");

        boolean isReIssue = false;
        if (expires_in != null) {
            Integer sec = expires_in.asInt();
            sec = sec / 60;
            sec = sec / 60;

            callVerifyResult.put("hr", sec);

            if (sec > 0 && sec < 24) {
                if (reIssue) {
                    isReIssue = callRefreshingAPI(channelId);
                }
            }
        } else {
            if (reIssue) {
                isReIssue = callRefreshingAPI(channelId);
            }
        }

        callVerifyResult.put("isReIssue", isReIssue);

        return callVerifyResult;
    }

    public static boolean callRefreshingAPI(String channelId) throws Exception {
        String client_id = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelID.toString(), true);
        String client_secret = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSecret.toString(), true);

        LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
        SystemConfigService systemConfigService = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class);
        ObjectNode callRefreshingResult = lineTokenApiService.callRefreshingAPI(client_id, client_secret);
        logger.info("callRefreshingResult:" + callRefreshingResult);

        JsonNode access_token = callRefreshingResult.get("access_token");

        if (access_token != null) {
            String token = access_token.asText();

            SystemConfig config = systemConfigService.findSystemConfig(channelId + "." + CONFIG_STR.ChannelToken.toString());
            config.setValue(token);
            systemConfigService.save(config);
            systemConfigService.clearData();
            SystemLogUtil.saveLogDebug(LOG_TARGET_ACTION_TYPE.TARGET_LineApi.toString(), LOG_TARGET_ACTION_TYPE.ACTION_RefreshingApi.toString(), SystemLog.SYSTEM_EVENT, callRefreshingResult, channelId);
            return true;
        }

        return false;
    }

    public static Integer sendPnpToLine(SendToBotModel sendToBotModel, List<String> deliveryTags) throws Exception {
        logger.debug("sendPnpToLine:" + sendToBotModel);

        if (SEND_TYPE.PUSH_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getPushMessage());
            try (CloseableHttpClient httpClient = HttpClientUtil.getSingleInstance()) {
                StringEntity entity = new StringEntity(postMsg, "UTF-8");
                entity.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

                // init Request
                HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED));
                logger.info("URI : " + requestPost.getURI());
                requestPost.setEntity(entity);

                String access_token = CoreConfigReader.getString(sendToBotModel.getChannelId(), CONFIG_STR.ChannelToken.toString(), true);
                requestPost.addHeader("Authorization", "Bearer " + access_token);
                logger.debug("Authorization : Bearer " + access_token);

                if (deliveryTags != null && deliveryTags.size() > 0) {
                    StringBuffer sb = new StringBuffer();

                    for (int i = 0; i < deliveryTags.size(); i++) {
                        sb.append(deliveryTags.get(i));

                        if (i < (deliveryTags.size() - 1)) {
                            sb.append(";;");
                        }
                    }

                    // 64 <= tag length <= 100，不夠則補空格
                    String deliveryTag = String.format("%1$-" + 64 + "s", sb.toString());

                    requestPost.addHeader("X-Line-Delivery-Tag", deliveryTag);
                    logger.debug("X-Line-Delivery-Tag : " + deliveryTag);
                }

                // execute Call
                HttpResponse clientResponse = httpClient.execute(requestPost);

                status = clientResponse.getStatusLine().getStatusCode();
                logger.info("clientResponse StatusCode : " + status);

                if (401 == status) {
                    callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
                    clearData();
                }

                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApi, start, status, postMsg, status + "");
                return status;
            } catch (Exception e) {
                String error = ErrorRecord.recordError(e, false);
                logger.error(error);
                SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApi, error, e.getMessage());
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApi_Error, start, status, postMsg, status + "");
                throw e;
            }
        }

        return null;
    }
}
