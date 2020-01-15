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
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CircleLineAccessApiService {
    public static final String LINE_API_SYNC = "LINE_API_SYNC";
    private static Map<String, List<LineMessagingService>> lineMessagingServiceMap = new HashMap<>();

    private ScheduledExecutorService flushTimer = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("Flush-Scheduled-%d")
                    .daemon(true).build()
    );

    private ScheduledExecutorService checkTokenTimer = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("Check-Token-Scheduled-%d")
                    .daemon(true).build()
    );

    public CircleLineAccessApiService() {
        flushTimer.scheduleWithFixedDelay(this::flushProcess, 120, 30, TimeUnit.SECONDS);
        checkTokenTimer.scheduleWithFixedDelay(this::checkTokenProcess, 150, 43_200, TimeUnit.SECONDS);
    }

    private void checkTokenProcess() {
        try {
            List<Object[]> channels = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%.ChannelId");
            if (channels != null && !channels.isEmpty()) {
                for (Object[] channel : channels) {
                    String configId = (String) channel[0];
                    log.info("callVerifyAPIAndIssueToken configId:" + configId);

                    if (StringUtils.isNotBlank(configId) && configId.indexOf(".") > 0) {
                        String[] split = configId.split("\\.");
                        if (split.length == 2) {
                            String channelId = split[0];
                            ObjectNode node = CircleLineAccessApiService.callVerifyAPIAndIssueToken(channelId, true);
                            log.info("callVerifyAPIAndIssueToken:" + channelId + ", isReIssue: " + node.get("isReIssue"));
                            log.info("callVerifyAPIAndIssueToken:" + ObjectUtil.objectToJsonStr(node));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private void flushProcess() {
        try {
            if (DataSyncUtil.isReSyncData(LINE_API_SYNC)) {
                lineMessagingServiceMap.clear();
                HttpClientUtil.clearData();
                DataSyncUtil.syncDataFinish(LINE_API_SYNC);
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }



    private static LineMessagingService getService(String channelId) {
        List<LineMessagingService> lineMessagingServices = lineMessagingServiceMap.get(channelId);
        if (lineMessagingServices == null || lineMessagingServices.isEmpty()) {

            String channelToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);

            if (lineMessagingServices == null) {
                lineMessagingServices = new ArrayList<>();
                lineMessagingServiceMap.put(channelId, lineMessagingServices);
            }

            if (lineMessagingServices.isEmpty()) {
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
                        log.error(ErrorRecord.recordError(e));
                    }
                    lineMessagingServices.add(lineMessagingService);
                }
            }
        }

        return randomOne(lineMessagingServices);
    }

    private static LineMessagingService randomOne(List<LineMessagingService> lineMessagingServices) {
        log.debug("randomOne Size:" + lineMessagingServices.size());

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
        log.debug("sendToLine:" + sendToBotModel);

        if (SEND_TYPE.REPLY_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getReplyMessage());
            try {

                Response<BotApiResponse> response = getService(sendToBotModel.getChannelId())
                        .replyMessage(sendToBotModel.getReplyMessage())
                        .execute();
                log.debug("{}", response.code());

                status = response.code();

                if (401 == status) {
                    callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
                    clearData();
                }

                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, start, status, postMsg, status + "");
                return response;
            } catch (Exception e) {
                String error = ErrorRecord.recordError(e, false);
                log.error(error);
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
                log.debug("{}", response.code());

                status = response.code();

                if (401 == status) {
                    callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
                    clearData();
                }

                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, start, status, postMsg, status + "");
                return response;
            } catch (Exception e) {
                String error = ErrorRecord.recordError(e, false);
                log.error(error);
                SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, error, e.getMessage());
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi_Error, start, status, postMsg, status + "");
                throw e;
            }
        }

        return null;
    }

    public static Response<ResponseBody> getImageFromLine(String channelId, String msgId) throws Exception {
        log.debug("getImageFromLine:" + msgId);

        Date start = new Date();
        int status = 0;

        try {

            Response<ResponseBody> response = getService(channelId)
                    .getMessageContent(msgId)
                    .execute();
            log.debug("{}", response.code());

            status = response.code();

            if (401 == status) {
                callVerifyAPIAndIssueToken(channelId, true);
                clearData();
            }

            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi, start, status, msgId, status + "");
            return response;
        } catch (Exception e) {
            String error = ErrorRecord.recordError(e, false);
            log.error(error);
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi, error, e.getMessage());
            SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi_Error, start, status, msgId, status + "");
            throw e;
        }
    }

    public static ObjectNode callVerifyAPIAndIssueToken(String channelId, boolean reIssue) throws Exception {
        String accessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);

        LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
        ObjectNode callVerifyResult = lineTokenApiService.callVerifyAPI(accessToken);
        log.info("callVerifyResult:" + callVerifyResult);

        JsonNode expiresIn = callVerifyResult.get("expires_in");

        boolean isReIssue = false;
        if (expiresIn != null) {
            int sec = expiresIn.asInt();
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
        String clientId = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelID.toString(), true);
        String clientSecret = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSecret.toString(), true);

        LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
        SystemConfigService systemConfigService = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class);
        ObjectNode callRefreshingResult = lineTokenApiService.callRefreshingAPI(clientId, clientSecret);
        log.info("callRefreshingResult:" + callRefreshingResult);

        JsonNode accessToken = callRefreshingResult.get("access_token");

        if (accessToken != null) {
            String token = accessToken.asText();

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
        log.debug("sendPnpToLine:" + sendToBotModel);

        if (SEND_TYPE.PUSH_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getPushMessage());
            try (CloseableHttpClient httpClient = HttpClientUtil.getSingleInstance()) {
                StringEntity entity = new StringEntity(postMsg, "UTF-8");
                entity.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

                // init Request
                HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED));
                log.info("URI : " + requestPost.getURI());
                requestPost.setEntity(entity);

                String accessToken = CoreConfigReader.getString(sendToBotModel.getChannelId(), CONFIG_STR.ChannelToken.toString(), true);
                requestPost.addHeader("Authorization", "Bearer " + accessToken);
                log.debug("Authorization : Bearer " + accessToken);

                if (deliveryTags != null && deliveryTags.size() > 0) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < deliveryTags.size(); i++) {
                        sb.append(deliveryTags.get(i));

                        if (i < (deliveryTags.size() - 1)) {
                            sb.append(";;");
                        }
                    }

                    // 64 <= tag length <= 100，不夠則補空格
                    String deliveryTag = String.format("%1$-" + 64 + "s", sb.toString());

                    requestPost.addHeader("X-Line-Delivery-Tag", deliveryTag);
                    log.debug("X-Line-Delivery-Tag : " + deliveryTag);
                }

                // execute Call
                HttpResponse clientResponse = httpClient.execute(requestPost);

                status = clientResponse.getStatusLine().getStatusCode();
                log.info("clientResponse StatusCode : " + status);

                if (401 == status) {
                    callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
                    clearData();
                }

                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApi, start, status, postMsg, status + "");
                return status;
            } catch (Exception e) {
                String error = ErrorRecord.recordError(e, false);
                log.error(error);
                SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApi, error, e.getMessage());
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApi_Error, start, status, postMsg, status + "");
                throw e;
            }
        }

        return null;
    }

    @PreDestroy
    public void cleanUp() {
        log.info("[DESTROY] LineAccessApiService cleaning up...");

        flushTimer.shutdown();
        checkTokenTimer.shutdown();
        log.info("[DESTROY] LineAccessApiService destroyed.");
    }
}
