package com.bcs.core.bot.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PreDestroy;

import okhttp3.Interceptor;
import okhttp3.ResponseBody;
import okhttp3.Request;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import retrofit2.Response;

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

@Service
public class LineAccessApiService {
	public static final String LINE_API_SYNC = "LINE_API_SYNC";

	private Timer flushTimer = new Timer();
	
	private Timer checkTokenTimer = new Timer();
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(LINE_API_SYNC);
				if(isReSyncData){
					lineMessagingServiceMap.clear();
					HttpClientUtil.clearData();
					DataSyncUtil.syncDataFinish(LINE_API_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public LineAccessApiService(){
		
		flushTimer.schedule(new CustomTask(), 120000, 30000);
		checkTokenTimer.schedule(new CustomTaskCheckToken(), 150_000, 43_200_000);
	}
	
	private class CustomTaskCheckToken extends TimerTask{
		
		@Override
		public void run() {

			try{
				List<Object[]> channels = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%.ChannelId");
				if(channels != null && channels.size() > 0){
					for(Object[] channel : channels){
						String configId = (String) channel[0];
						logger.info("callVerifyAPIAndIssueToken configId:" + configId);
						
						if(StringUtils.isNotBlank(configId) && configId.indexOf(".") > 0){
							String[] split = configId.split("\\.");
							if(split != null && split.length == 2){
								String channelId = split[0];
								ObjectNode node = LineAccessApiService.callVerifyAPIAndIssueToken(channelId, true);
								logger.info("callVerifyAPIAndIssueToken:" + channelId + ", isReIssue: " + node.get("isReIssue"));
								logger.info("callVerifyAPIAndIssueToken:" + ObjectUtil.objectToJsonStr(node));
							}
						}
					}
				}
			}
			catch(Throwable e){
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

	/** Logger */
	private static Logger logger = Logger.getLogger(LineAccessApiService.class);
	
	private static Map<String, List<LineMessagingService>> lineMessagingServiceMap = new HashMap<String, List<LineMessagingService>>();
	
	private static LineMessagingService getService(String ChannelId,String ChannelName){
		String Channel = ChannelId + ChannelName;
		
		List<LineMessagingService> lineMessagingServices = lineMessagingServiceMap.get(Channel);
		if(lineMessagingServices == null || lineMessagingServices.size() == 0){
			String channelToken = CoreConfigReader.getString(ChannelId, CONFIG_STR.ChannelToken.toString(), true);
			final String serviceCode = CoreConfigReader.getString(ChannelName, CONFIG_STR.ChannelServiceCode.toString(), true);
			
			if(lineMessagingServices == null){
				lineMessagingServices = new ArrayList<LineMessagingService>();
				lineMessagingServiceMap.put(Channel, lineMessagingServices);
			}

			if(lineMessagingServices.size() == 0){
				for(int i = 0; i < 300; i++){
					LineMessagingServiceBuilder builder = LineMessagingServiceBuilder.create(channelToken);
					
					Interceptor interceptor = new Interceptor() {
						@Override
						public okhttp3.Response intercept(Chain chain) throws IOException {
							 Request request = chain.request().newBuilder()
		                               .addHeader(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode)
		                               .build();
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
						if(StringUtils.isNotBlank(proxyUrl)){
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
	
	private static LineMessagingService randomOne(List<LineMessagingService> lineMessagingServices){
		logger.debug("randomOne Size:" + lineMessagingServices.size());

        int index = new Random().nextInt(lineMessagingServices.size());
        return lineMessagingServices.get(index);
	}
	
	public static void clearData(){
		for(List<LineMessagingService> list : lineMessagingServiceMap.values()){
			list.clear();
		}
		lineMessagingServiceMap.clear();
		HttpClientUtil.clearData();
		DataSyncUtil.settingReSync(LINE_API_SYNC);
	}

	public static Response<BotApiResponse> sendToLine(SendToBotModel sendToBotModel) throws Exception{
		logger.info("sendToBotModel:" + sendToBotModel);
		
		String ChannelId = sendToBotModel.getChannelId();
		String ChannelName = sendToBotModel.getChannelName();
		logger.info("ChannelId:" + ChannelId);
		logger.info("ChannelName:" + ChannelName);
		
		if(ChannelName.equals(CONFIG_STR.InManualReplyButNotSendMsg.toString())){
			throw new BcsNoticeException("使用者在真人客服無法推播");
		}
		if(SEND_TYPE.REPLY_MSG.equals(sendToBotModel.getSendType())){

			Date start = new Date();
			int status = 0;
			
			String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getReplyMessage());
			try {
				Response<BotApiResponse> response = getService(ChannelId,ChannelName)
				        .replyMessage(sendToBotModel.getReplyMessage())
				        .execute();
				logger.debug(response.code());
				
				status = response.code();

				if(401 == status){
					callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
					clearData();
				}
				
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, start, status, postMsg, status + "");
				return response;
			}
			catch(Exception e){
				String error = ErrorRecord.recordError(e, false);
				logger.error(error);
				SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, error, e.getMessage());
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi_Error, start, status, postMsg, status + "");
				throw e;
			}
		}
		else if(SEND_TYPE.PUSH_MSG.equals(sendToBotModel.getSendType())){

			Date start = new Date();
			int status = 0;
			
			String postMsg = ObjectUtil.objectToJsonStr(sendToBotModel.getPushMessage());
			try {

				Response<BotApiResponse> response = getService(ChannelId, ChannelName)
				        .pushMessage(sendToBotModel.getPushMessage())
				        .execute();
				logger.debug(response.code());
				
				status = response.code();

				if(401 == status){
					callVerifyAPIAndIssueToken(sendToBotModel.getChannelId(), true);
					clearData();
				}
				
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, start, status, postMsg, status + "");
				return response;
			}
			catch(Exception e){
				String error = ErrorRecord.recordError(e, false);
				logger.error(error);
				SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi, error, e.getMessage());
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApi_Error, start, status, postMsg, status + "");
				throw e;
			}
		}
		
		return null;
	}
	
	public static Response<ResponseBody> getImageFromLine(String channelId,String channelName, String msgId) throws Exception{
		logger.debug("getImageFromLine:" + msgId);

		Date start = new Date();
		int status = 0;
		
		try {
			
			Response<ResponseBody> response = getService(channelId,channelName)
			                .getMessageContent(msgId)
			                .execute();
			logger.debug(response.code());
			
			status = response.code();

			if(401 == status){
				callVerifyAPIAndIssueToken(channelId, true);
				clearData();
			}
			
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi, start, status, msgId, status + "");			    
		    return response;
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApi_Error, start, status, msgId, status + "");
			throw e;
		}
	}
	
	public static ObjectNode callVerifyAPIAndIssueToken(String channelId, boolean reIssue) throws Exception{
		String access_token = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
		
		LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
		ObjectNode callVerifyResult = lineTokenApiService.callVerifyAPI(access_token);
		logger.info("callVerifyResult:" + callVerifyResult);

		JsonNode expires_in = callVerifyResult.get("expires_in");
		
		boolean isReIssue = false;
		if(expires_in != null){
			Integer sec = expires_in.asInt();
			sec = sec/60;
			sec = sec/60;
			
			callVerifyResult.put("hr", sec);
			
			if(sec > 0 && sec < 24){
				if(reIssue){
					isReIssue = callRefreshingAPI(channelId);
				}
			}
		}
		else{
			if(reIssue){
				isReIssue = callRefreshingAPI(channelId);
			}
		}

		callVerifyResult.put("isReIssue", isReIssue);
		
		return callVerifyResult;
	}
	
	public static boolean callRefreshingAPI(String channelId) throws Exception{
		String client_id = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelID.toString(), true);
		String client_secret = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSecret.toString(), true);

		LineTokenApiService lineTokenApiService = ApplicationContextProvider.getApplicationContext().getBean(LineTokenApiService.class);
		SystemConfigService systemConfigService = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class);
		ObjectNode callRefreshingResult = lineTokenApiService.callRefreshingAPI(client_id, client_secret);
		logger.info("callRefreshingResult:" + callRefreshingResult);

		JsonNode access_token = callRefreshingResult.get("access_token");

		if(access_token != null){
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
}
