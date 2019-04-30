package com.bcs.core.smartrobot.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
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
import com.bcs.core.utils.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class SmartRobotApiService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SmartRobotApiService.class);

	public ArrayNode callSmartRobotApi(String sessionId, String question) throws Exception{
		Date start = new Date();
		return this.callSmartRobotApi(sessionId, question, start, 0);
	}
	
	private ArrayNode callSmartRobotApi(String sessionId, String question, Date start, int retryCount) throws Exception{
		logger.info("callSmartRobotApi:" + sessionId);

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
	
			ObjectNode content = (new ObjectMapper()).createObjectNode();
			content.put("channel", "HPI");
			content.put("platform", "LINE");
			content.put("sessionId", sessionId);
			if(StringUtils.isBlank(question)){
				content.put("question", "-");
			}
			else{
				content.put("question", question);
			}
			
			String postMsg = ObjectUtil.objectToJsonStr(content);
			
		    StringEntity entity = new StringEntity(postMsg, "UTF-8");
		    entity.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
		    
			// init Request
			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.SMART_ROBOT_API));
			logger.info("URI : " + requestPost.getURI());
			requestPost.setEntity(entity);
	
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
			logger.debug("clientResponse result : " + result);
			
			requestPost.releaseConnection();

			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_API, LOG_TARGET_ACTION_TYPE.ACTION_SMARTROBOT_API, start, status, postMsg, status + "");
			return (ArrayNode)(new ObjectMapper()).readTree(result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_API, LOG_TARGET_ACTION_TYPE.ACTION_SMARTROBOT_API, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_API, LOG_TARGET_ACTION_TYPE.ACTION_SMARTROBOT_API_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callSmartRobotApi(sessionId, question, start, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
}
