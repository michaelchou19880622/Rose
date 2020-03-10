package com.bcs.core.api.service;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.bcs.core.api.service.model.LiveChatResponse;
import com.bcs.core.api.service.model.LiveChatStartResponse;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LIVE_CHAT_WORDING;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.exception.LiveChatException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LiveChatWordingUtil;
import com.bcs.core.utils.RestfulUtil;

@Service
public class LiveChatApiService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LiveChatApiService.class);
	
	@Autowired
	private UserLiveChatService userLiveChatService;
	
	/*
	 * === 連結真人客服 ===
	 */
	public LiveChatStartResponse startChat(String UID, String category) throws Exception {
		Date start = new Date();
		try {
				String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_START_API_URL.toString());
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				
				MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
				map.add("ICSPID", "");
				map.add("lat", "25.037531");
				map.add("lng", "121.5639969");
				map.add("qaCategory", category);
				map.add("eservice", CoreConfigReader.getString(CONFIG_STR.LIVECHAT_CHANNEL.toString(), true));
				map.add("mediaTyoe", "");
				map.add("et", "message");
				map.add("forward", "linecc");
				map.add("microphone", "false");
				map.add("ip", "127.0.0.1");
				map.add("userId", UID);
                map.add("timestamp", new Timestamp(System.currentTimeMillis()).toString());
				
				HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);
				
				RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));
				
				JSONObject responseObject = restfulUtil.execute(); // 傳送 http request
				String forwardUrl = responseObject.getString("forwardTo");
				String returnMessage = responseObject.getString("sequence");
				JSONObject chatInfo = responseObject.getJSONObject("chat_info");
				
				if (responseObject.getJSONArray("error").length() > 0) {
					throw new Exception(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LIVE_CHAT_START_FAIL.toString()));
				} else {
					LiveChatStartResponse result = new LiveChatStartResponse();
					result.setCategory(chatInfo.getString("category"));
					result.setFowardURL(forwardUrl);
					result.setHash(chatInfo.getString("hash"));
					result.setReturnMessage(returnMessage);
					
					try {
						result.setChatId(chatInfo.getLong("id"));
						result.setStatus(chatInfo.getInt("status"));
						result.setTimestamp(new Date(chatInfo.getLong("time") * 1000));
					} catch (Exception e) {
						result.setChatId(Long.parseLong(chatInfo.getString("id")));
						result.setStatus(Integer.parseInt(chatInfo.getString("status")));
						result.setTimestamp(new Date(Long.parseLong(chatInfo.getString("time")) * 1000));
					}
					
					SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_START_CHAT, start, 200, responseObject.toString(), 200 + "");
					return result;
				}
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_START_CHAT, error, e.getMessage());
			throw new BcsNoticeException(e.getMessage());
		}
	}
	
	/*
	 * === 傳送訊息給真人客服 === String UID String message
	 */
	public LiveChatResponse addMessage(String UID, String message) throws BcsNoticeException {
		Date start = new Date();
		try {
			/* 撈取 User 的 chatId 以及 hash 字串，組合成 API 的 URL */
			UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.IN_PROGRESS);
			
			if(userLiveChat==null){
				throw new BcsNoticeException(UID +" is not in progress");
			}
			
			String chatId = userLiveChat.getChatId().toString();
			String hash = userLiveChat.getHash();
			String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_ADD_MESSAGE_API_URL.toString()) + chatId + "/" + hash;
			
			/* Call 真人客服 API，將訊息轉發給客服人員 */
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("msg", message);
			map.add("STT", "false");
			map.add("TTS", "false");
			
			HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);
			
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));
			
			JSONObject responseObject = restfulUtil.execute(); // 傳送 http request
			
			LiveChatResponse result = new LiveChatResponse();
			
			result.setError(responseObject.getString("error").equals("f") ? false : true);
			result.setTextMessage((responseObject.get("out") == null) ? null : responseObject.get("out").toString());
			
			if (result.getError()) {
				JSONObject errorMessage = new JSONObject(responseObject.get("r").toString());
				result.setErrorMessage(errorMessage.getString("originalMessage"));
				SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_ADD_MESSAGE, errorMessage, result.getErrorMessage());
				throw new BcsNoticeException(result.getErrorMessage());
			}
			
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_ADD_MESSAGE, start, 200, responseObject.toString(), 200 + "");			
			return result;
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_ADD_MESSAGE, error, e.getMessage());
			throw new BcsNoticeException(error);
		}
	}
	
	public LiveChatResponse giveUpChat(Long chatId,String hash, Integer status) throws BcsNoticeException {
		try {
			String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_GIVE_UP_API_URL.toString()) + chatId.toString() + "/" + hash + "/" + status.toString();
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
			
			HttpEntity<?> httpEntity = new HttpEntity<>(headers);
			
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.GET, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));
			
			JSONObject responseObject = restfulUtil.execute();
			
			LiveChatResponse result = new LiveChatResponse();
			
			result.setError(responseObject.getString("error").equals("false") ? false : true);			
			
			if (!result.getError()) {
				result.setTextMessage(responseObject.getString("result"));
			} else {
				result.setErrorMessage(responseObject.getString("result"));
				
				throw new BcsNoticeException(result.getErrorMessage());
			}
			
			return result;
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_ADD_MESSAGE, error, e.getMessage());
			throw new BcsNoticeException(error);
		}
	}
	
	public void resetChatFlow(String UID) throws Exception {
		String apikey = CoreConfigReader.getString(CONFIG_STR.GATEWAY_API_KEY.toString(), true);
		String channel = CoreConfigReader.getString(CONFIG_STR.GATEWAY_CHANNEL.toString(), true);
		String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_RESET_API_URL.toString());
		url = UriComponentsBuilder.fromUriString(url)
				.queryParam("ch", channel)
				.queryParam("apikey", apikey)
				.queryParam("id", UID)
				.queryParam("q", "weather")
				.queryParam("status", "chatrestart")
				.queryParam("testMode", "true")
				.build()
				.toUriString();
		
		/* 設定 request headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<?> httpEntity = new HttpEntity<>(headers);
		
		RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.GET, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));

		restfulUtil.execute();
	}
	
	/*
	 * === 離開真人客服 === String UID
	 */
	public LiveChatResponse closeChat(String chatId,String hash,String UID) throws BcsNoticeException {
		Date start = new Date();
		try {
			String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_CLOSE_API_URL.toString()) + chatId + "/" + hash;
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("status", "2");
			
			HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);
			
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));
			
			JSONObject responseObject = restfulUtil.execute(); // 傳送 http request
			
			LiveChatResponse result = new LiveChatResponse();
			
			result.setError(responseObject.getString("error").equals("f") ? false : true);
			result.setTextMessage((responseObject.get("out") == null) ? null : responseObject.get("out").toString());
			
			if (result.getError() && result.getTextMessage()!=null) {
				JSONObject errorMessage = new JSONObject(responseObject.get("r").toString());
				result.setErrorMessage(errorMessage.getString("originalMessage"));
				SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_CLOSE_CHAT, errorMessage, result.getErrorMessage());
			}
			
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_CLOSE_CHAT, start, 200, responseObject.toString(), 200 + "");			
			return result;
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_TaishinApi, LOG_TARGET_ACTION_TYPE.ACTION_TaishinApi_CLOSE_CHAT, error, e.getMessage());
			throw new BcsNoticeException(error);
		}	
	}
	
	/*
	 * 留言給客服專員
	 */
	public LiveChatStartResponse leaveMessage(String UID, UserLiveChat userLiveChat) throws Exception {
		 String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_LEAVE_MESSAGE_API_URL.toString());
		 HttpHeaders headers = new HttpHeaders();
		 
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("ICSPID", "");
		map.add("lat", "25.037531");
		map.add("lng", "121.5639969");
		map.add("qaCategory", userLiveChat.getCategory());
		map.add("eservice", CoreConfigReader.getString(CONFIG_STR.LIVECHAT_CHANNEL.toString(), true));
		map.add("ip", "127.0.0.1");
		map.add("userId", UID);
		map.add("message", userLiveChat.getMesssage());
        map.add("timestamp", new Timestamp(System.currentTimeMillis()).toString());
		
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		
		RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));
		
		JSONObject responseObject = restfulUtil.execute(); // 傳送 http request
		
		JSONArray error = responseObject.getJSONArray("error");
		
		if(error.length() > 0) {
			throw new LiveChatException("[LiveChatApiService] LeaveMessage: " + error.getString(0));
		} else {
			JSONObject chatInfo = responseObject.getJSONObject("chat_info");
			LiveChatStartResponse result = new LiveChatStartResponse();
			
			result.setCategory(chatInfo.getString("category"));
			result.setFowardURL(responseObject.getString("forwardTo"));
			result.setHash(chatInfo.getString("hash"));
			result.setReturnMessage(responseObject.getString("sequence"));
			
			try {
				result.setChatId(chatInfo.getLong("id"));
				result.setStatus(chatInfo.getInt("status"));
				result.setTimestamp(new Date(chatInfo.getLong("time") * 1000));
			} catch (Exception e) {
				result.setChatId(Long.parseLong(chatInfo.getString("id")));
				result.setStatus(Integer.parseInt(chatInfo.getString("status")));
				result.setTimestamp(new Date(Long.parseLong(chatInfo.getString("time")) * 1000));
			}
			
			return result;
		}
	}
}