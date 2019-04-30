package com.bcs.core.bot.scheduler.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bcs.core.api.service.LineSwitchApiService;
import com.bcs.core.api.service.model.LiveChatStartResponse;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.RestfulUtil;

public class CheckUserLiveChatStatus implements Job {
	/** Logger */
	private static Logger logger = Logger.getLogger(CheckUserLiveChatStatus.class);
	
	UserLiveChatService userLiveChatService = ApplicationContextProvider.getApplicationContext().getBean(UserLiveChatService.class);
	LineSwitchApiService lineSwitchApiService = ApplicationContextProvider.getApplicationContext().getBean(LineSwitchApiService.class);
			
	public void execute(JobExecutionContext context) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String autoReplyChannelName = CoreConfigReader.getString(CONFIG_STR.AUTOREPLY_CHANNEL_NAME.toString(), true);
			List<UserLiveChat> userList = userLiveChatService.findWaitingAndInProgressUser();	// 找出「等待中」以及「在客服流程中」的使用者
			
			logger.info("[Schedule] start at " + sdf.format(new Date()));
			
			for(UserLiveChat user : userList) {				
				Long chatId = user.getChatId();
				String hash = user.getHash();
				String url = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_CHECK_API_URL.toString()) + chatId.toString() + "/" + hash;
				
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
				
				HttpEntity<?> httpEntity = new HttpEntity<>(headers);
				
				RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.GET, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));
				
				JSONObject responseObject = restfulUtil.execute();
				
				if(!Boolean.valueOf(responseObject.getString("error"))) {
					if(user.getStatus().equals(UserLiveChat.IN_PROGRESS)) {
						if(responseObject.getString("chat_status").equals(String.valueOf(LiveChatStartResponse.NON_OFFICE_HOUR))) {
							lineSwitchApiService.executeSwitch(CoreConfigReader.getString(autoReplyChannelName, "DestinationId", true), user.getUID(), "");
							
							user.setStatus(UserLiveChat.FINISH);
							user.setModifyTime(new Date());
							
							userLiveChatService.save(user);
						}
					} else if(user.getStatus().equals(UserLiveChat.WAITING)) {
						if(responseObject.getString("chat_status").equals(String.valueOf(LiveChatStartResponse.NON_OFFICE_HOUR))) {
							user.setStatus(UserLiveChat.DISCARD);
							user.setModifyTime(new Date());
							
							userLiveChatService.save(user);
						}
					}
				} else {
					throw new Exception("[Schedule] Encounter problems when checking user's live chat status.");
				}
			}
		} catch(Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
		}
	}
}