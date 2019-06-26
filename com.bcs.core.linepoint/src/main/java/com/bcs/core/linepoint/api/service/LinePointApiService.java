package com.bcs.core.linepoint.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bcs.core.api.service.model.PushApiModel;
//import com.bcs.core.db.service.MsgAudienceMatchSendService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
//import com.bcs.core.interactive.service.LocationInteractiveService;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.record.service.CatchHandleMsgReceiveTimeout;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;
import com.bcs.core.utils.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class LinePointApiService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointApiService.class);
	
	public ObjectNode callLinePointAPI(String clientId, String memberId, String orderKey, Long amount, String channelToken) throws Exception{
		Date start = new Date();
		return this.callLinePointAPI(start, clientId, memberId, orderKey, amount, channelToken, 0);
	}
	
	public ObjectNode callLinePointAPI(Date start, String clientId, String memberId, String orderKey, Long amount,
			String channelToken, int retryCount) throws Exception{
		logger.info("LineAudienceMatchApiService callMissionStickersAPI");

		int status = 0;
		try{
			logger.info("LineAudienceMatchApiService 1111");
			
			HttpClient httpClient = HttpClientUtil.generateClient();
			logger.info("LineAudienceMatchApiService 2222");
			// Request Body
			String applicationTime = String.valueOf(Instant.now().toEpochMilli());
			ObjectNode requestContent = new ObjectMapper().createObjectNode();
			requestContent.put("clientId", clientId);
			requestContent.put("memberId", memberId);
			requestContent.put("orderKey", orderKey);
			requestContent.put("applicationTime", applicationTime);
			requestContent.put("amount", amount);
						
			logger.info("LineAudienceMatchApiService requestContent.toString():" + requestContent.toString());
			
		    StringEntity entity = new StringEntity(requestContent.toString(), "UTF-8");
		    entity.setContentType(MediaType.APPLICATION_JSON_VALUE);
		    		  
		    logger.info("LineAudienceMatchApiService setContentType");
		    
			// init Request
			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(""));
			// CONFIG_STR.LINE_POINT_API
			logger.info("LineAudienceMatchApiService URI : " + requestPost.getURI());
			requestPost.setHeader("Authorization", "Bearer " +  channelToken);
			requestPost.setEntity(entity);
			logger.debug("LineAudienceMatchApiService requestPost : " + requestPost.toString());
			
			// print requestPost
			logger.debug("LineAudienceMatchApiService requestContent : " + requestContent);
	
			// execute Call
			HttpResponse clientResponse = httpClient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.info("LineAudienceMatchApiService clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("LineAudienceMatchApiService clientResponse result : " + result);
			
			requestPost.releaseConnection();

			// MsgAudienceMatchSendService amService =ApplicationContextProvider.getApplicationContext().getBean(MsgAudienceMatchSendService.class);
			//update Status
			if (status == 200) {
				logger.debug("LineAudienceMatchApiService clientResponse Success result : " + result + "!!!");	
				//for(String uid: uids)
				//	amService.updateStautsAndRespCodeByPhoneAndId(msgSendId, 8, status, uid);
			}
			else {
				logger.debug("LineAudienceMatchApiService clientResponse Failure result : " + result + "!!!");				
			}
			// SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_MissionStickersApi, start, status, requestContent.toString(), status + "");
			return (ObjectNode)(new ObjectMapper()).readTree(result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			// SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_MissionStickersApi, error, e.getMessage());
			// SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_MissionStickersApi_Error, start, status, error, status + "");
			if(retryCount < 1){
				return this.callLinePointAPI(start, clientId, memberId, orderKey, amount, channelToken, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
}
