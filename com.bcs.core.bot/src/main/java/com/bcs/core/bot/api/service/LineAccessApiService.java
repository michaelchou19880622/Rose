package com.bcs.core.bot.api.service;

import com.bcs.core.api.service.LineTokenApiService;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
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
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ???
 */
@Slf4j
@Service
public class LineAccessApiService {
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

    public LineAccessApiService() {
        flushTimer.scheduleWithFixedDelay(this::flushProcess, 120, 30, TimeUnit.SECONDS);
        checkTokenTimer.scheduleWithFixedDelay(this::checkTokenProcess, 150, 43_200, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void cleanUp() {
        log.info("[DESTROY] LineAccessApiService cleaning up...");

        flushTimer.shutdown();
        checkTokenTimer.shutdown();
        log.info("[DESTROY] LineAccessApiService destroyed.");
    }

    private void checkTokenProcess() {
        try {
            List<Object[]> channels = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%.ChannelId");
            if (channels != null && !channels.isEmpty()) {
                for (Object[] channel : channels) {
                    String configId = (String) channel[0];
                    log.info("callVerifyAPIAndIssueToken configId:" + configId);

                    if (StringUtils.isNotBlank(configId)) {
                        String[] split = configId.split("\\.");
                        if (split.length == 2) {
                            String channelId = split[0];
                            ObjectNode node = LineAccessApiService.callVerifyAPIAndIssueToken(channelId, true);
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

    private static LineMessagingService getService(String channelId, String channelName) {
        log.info("getService");
        
        log.info("channelId = {}", channelId);
        log.info("channelName = {}", channelName);
        
        String channel = channelId + channelName;
        log.info("channel = {}", channel);

        List<LineMessagingService> lineMessagingServices = lineMessagingServiceMap.get(channel);
        if (lineMessagingServices == null || lineMessagingServices.isEmpty()) {
            String channelToken = CoreConfigReader.getString(channelId, CONFIG_STR.CHANNEL_TOKEN.toString(), true);
            log.info("channelToken = " + channelToken);

            if (lineMessagingServices == null) {
                lineMessagingServices = new ArrayList<>();
                lineMessagingServiceMap.put(channel, lineMessagingServices);
            }

            if (lineMessagingServices.isEmpty()) {
                for (int i = 0; i < 300; i++) {
                    LineMessagingServiceBuilder builder = LineMessagingServiceBuilder.create(channelToken);

                    Interceptor interceptor = new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder().build();
                            log.info("request.headers = {}", request.headers());                                                        
                            return chain.proceed(request);
                        }
                    };

                    builder.addInterceptor(interceptor);
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
    
    private static LineMessagingService getServiceWithServiceCode(String channelId, String channelName) {
        log.info("getServiceWithServiceCode");
        
        log.info("channelId = {}", channelId);
        log.info("channelName = {}", channelName);
    	
        String channel = channelId + channelName;
        log.info("Channel = {}", channel);

        List<LineMessagingService> lineMessagingServices = lineMessagingServiceMap.get(channel);
        if (lineMessagingServices == null || lineMessagingServices.isEmpty()) {
            String channelToken = CoreConfigReader.getString(channelId, CONFIG_STR.CHANNEL_TOKEN.toString(), true);
            log.info("channelToken = {}", channelToken);

            final String serviceCode = CoreConfigReader.getString(channelName, CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
            log.info("serviceCode = {}", serviceCode);

            if (lineMessagingServices == null) {
                lineMessagingServices = new ArrayList<>();
                lineMessagingServiceMap.put(channel, lineMessagingServices);
            }

            if (lineMessagingServices.isEmpty()) {
                for (int i = 0; i < 300; i++) {
                    LineMessagingServiceBuilder builder = LineMessagingServiceBuilder.create(channelToken);

                    Interceptor interceptor = new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder()
                                    .addHeader(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode)
                                    .build();
                            log.info("request.headers = {}", request.headers());                            
                            return chain.proceed(request);
                        }
                    };

                    builder.addInterceptor(interceptor);
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
        log.debug("Get Random One, index = " + index);
		
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
        log.info("sendToLine");
		log.info("sendToBotModel = {}", sendToBotModel);
		log.info("sendToBotModel.getSendType() = {}", sendToBotModel.getSendType());

        String channelId = sendToBotModel.getChannelId();
        String channelName = sendToBotModel.getChannelName();
		log.info("channelId = {}", channelId);
		log.info("channelName = {}", channelName);

        if (channelName.equals(CONFIG_STR.IN_MANUAL_REPLY_BUT_NOT_SEND_MSG.toString())) {
            throw new BcsNoticeException("使用者在真人客服無法推播");
        }

        if (SEND_TYPE.REPLY_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getReplyMessage());
            log.info("postMsg = {}", postMsg);
            
            try {
                Response<BotApiResponse> response;
                
                if (channelName.equals(CONFIG_STR.MANUAL_REPLY.toString()) || channelName.equals(CONFIG_STR.AUTO_REPLY.toString())) {
                    response = getServiceWithServiceCode(channelId, channelName)
                            .replyMessage(sendToBotModel.getReplyMessage())
                            .execute();
                } else {
                    response = getService(channelId, channelName)
                            .replyMessage(sendToBotModel.getReplyMessage())
                            .execute();
				}
                log.info("response = {}", response);

                status = response.code();
                log.info("status = {}", status);

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
			log.info("postMsg : {}", postMsg);
            
            try {

                Response<BotApiResponse> response;
                
                if (channelName.equals(CONFIG_STR.MANUAL_REPLY.toString()) || channelName.equals(CONFIG_STR.AUTO_REPLY.toString())) {
                    response = getServiceWithServiceCode(channelId, channelName)
    	                        .pushMessage(sendToBotModel.getPushMessage())
    	                        .execute();
                } else {
                    response = getService(channelId, channelName)
    	                        .pushMessage(sendToBotModel.getPushMessage())
    	                        .execute();
				}
                log.info("response = {}", response);

                status = response.code();
                log.info("status = {}", status);

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
    
    public static Response<BotApiResponse> sendToLineWithServiceCode(SendToBotModel sendToBotModel) throws Exception {
        log.info("sendToLineWithServiceCode");
		log.info("sendToBotModel = {}", sendToBotModel);
		log.info("sendToBotModel.getSendType() = {}", sendToBotModel.getSendType());

        String channelId = sendToBotModel.getChannelId();
        String channelName = sendToBotModel.getChannelName();
        log.info("ChannelId = {}", channelId);
        log.info("ChannelName = {}", channelName);

        boolean isUsingServiceCode = false;
        if (channelName.equals(CONFIG_STR.MANUAL_REPLY.toString()) || channelName.equals(CONFIG_STR.AUTO_REPLY.toString())) {
        	isUsingServiceCode = true;
        }
        log.info("isUsingServiceCode = {}", isUsingServiceCode);
        
        if (channelName.equals(CONFIG_STR.IN_MANUAL_REPLY_BUT_NOT_SEND_MSG.toString())) {
            throw new BcsNoticeException("使用者在真人客服無法推播");
        }
        
        if (SEND_TYPE.REPLY_MSG.equals(sendToBotModel.getSendType())) {

            Date start = new Date();
            int status = 0;

            String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getReplyMessage());
            log.info("postMsg = {}", postMsg);
            
            try {
                Response<BotApiResponse> response;
                
                if (isUsingServiceCode) {
                    response = getServiceWithServiceCode(channelId, channelName)
                            .replyMessage(sendToBotModel.getReplyMessage())
                            .execute();
                } else {
                    response = getService(channelId, channelName)
                            .replyMessage(sendToBotModel.getReplyMessage())
                            .execute();
				}
                log.info("response = {}", response);

                status = response.code();
                log.info("status = {}", status);

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
            log.info("postMsg = {}", postMsg);
            
            try {

                Response<BotApiResponse> response;

                if (isUsingServiceCode) {
                    response = getServiceWithServiceCode(channelId, channelName)
    	                        .pushMessage(sendToBotModel.getPushMessage())
    	                        .execute();
                } else {
                    response = getService(channelId, channelName)
    	                        .pushMessage(sendToBotModel.getPushMessage())
    	                        .execute();
				}
                log.info("response = {}", response);

                status = response.code();
                log.info("status = {}", status);

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

    public static Response<ResponseBody> getImageFromLine(String channelId, String channelName, String msgId) throws Exception {
        log.debug("getImageFromLine:" + msgId);

        Date start = new Date();
        int status = 0;

        try {

			Response<ResponseBody> response = getService(channelId, channelName).getMessageContent(msgId).execute();
            log.debug("response.code() = {}", response.code());

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
        String accessToken = CoreConfigReader.getString(channelId, CONFIG_STR.CHANNEL_TOKEN.toString(), true);

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
        String clientId = CoreConfigReader.getString(channelId, CONFIG_STR.CHANNEL_ID.toString(), true);
        String clientSecret = CoreConfigReader.getString(channelId, CONFIG_STR.CHANNEL_SECRET.toString(), true);

        LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
        SystemConfigService systemConfigService = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class);
        ObjectNode callRefreshingResult = lineTokenApiService.callRefreshingAPI(clientId, clientSecret);
        log.info("callRefreshingResult:" + callRefreshingResult);

        JsonNode accessToken = callRefreshingResult.get("access_token");

        if (accessToken != null) {
            String token = accessToken.asText();

            SystemConfig config = systemConfigService.findSystemConfig(channelId + "." + CONFIG_STR.CHANNEL_TOKEN.toString());
            config.setValue(token);
            systemConfigService.save(config);
            systemConfigService.clearData();
            SystemLogUtil.saveLogDebug(LOG_TARGET_ACTION_TYPE.TARGET_LineApi.toString(), LOG_TARGET_ACTION_TYPE.ACTION_RefreshingApi.toString(), SystemLog.SYSTEM_EVENT, callRefreshingResult, channelId);
            return true;
        }

        return false;
    }
}
