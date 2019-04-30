package com.bcs.core.smartrobot.service;

import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class SmartRobotBotApiService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SmartRobotBotApiService.class);

	public ObjectNode callSmartRobotBotApi(String postMsg, String channelSignature) throws Exception{
		Date start = new Date();
		return this.callSmartRobotBotApi(postMsg, channelSignature, start, 0);
	}
	
	private ObjectNode callSmartRobotBotApi(String postMsg, String channelSignature, Date start, int retryCount) throws Exception{
		logger.info("callSmartRobotBotApi:" + postMsg);

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
			
		    StringEntity entity = new StringEntity(postMsg, "UTF-8");
		    entity.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
		    
			// init Request
			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.SMART_ROBOT_BOT_API));
			logger.info("URI : " + requestPost.getURI());
			requestPost.setEntity(entity);
			requestPost.addHeader("X-Line-Signature", channelSignature);
	
			// print requestPost
			logger.debug("postMsg : " + postMsg);
	
			// execute Call
			HttpResponse clientResponse = httpClient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.info("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.info("clientResponse result : " + result);
			
			requestPost.releaseConnection();

			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_API, LOG_TARGET_ACTION_TYPE.ACTION_SMARTROBOT_BOT_API, start, status, postMsg, status + "");
			return (ObjectNode)(new ObjectMapper()).readTree(result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_API, LOG_TARGET_ACTION_TYPE.ACTION_SMARTROBOT_BOT_API, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_API, LOG_TARGET_ACTION_TYPE.ACTION_SMARTROBOT_BOT_API_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callSmartRobotBotApi(postMsg, channelSignature, start, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
}
