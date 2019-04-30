package com.bcs.core.api.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.jcodec.common.StringUtils;
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
public class LineTokenApiService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LineTokenApiService.class);

	public ObjectNode callVerifyAPI(String access_token) throws Exception{
		Date start = new Date();
		return this.callVerifyAPI(start, access_token, 0);
	}

	public ObjectNode callVerifyAPI(Date start, String access_token, int retryCount) throws Exception{
		logger.info("callVerifyAPI");

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
	
			List<String> list = new ArrayList<String>();
			list.add("access_token=" + URLEncoder.encode(access_token, "UTF-8"));
			
			String postMsg = StringUtils.join(list.toArray(), "&");
			
		    StringEntity entity = new StringEntity(postMsg, "UTF-8");
		    entity.setContentType("application/x-www-form-urlencoded");
		    
			// init Request
			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_VERIFY));
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

			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_VerifyApi, start, status, postMsg, status + "");
			return (ObjectNode)(new ObjectMapper()).readTree(result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_VerifyApi, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_VerifyApi_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callVerifyAPI(start, access_token, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}

	public ObjectNode callRefreshingAPI(String client_id, String client_secret) throws Exception{
		Date start = new Date();
		return this.callRefreshingAPI(start, client_id, client_secret, 0);
	}

	public ObjectNode callRefreshingAPI(Date start, String client_id, String client_secret, int retryCount) throws Exception{
		logger.info("callRefreshingAPI");

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
	
			List<String> list = new ArrayList<String>();
			list.add("grant_type=client_credentials");
			list.add("client_id=" + client_id);
			list.add("client_secret=" + client_secret);
			
			String postMsg = StringUtils.join(list.toArray(), "&");
			
		    StringEntity entity = new StringEntity(postMsg, "UTF-8");
		    entity.setContentType("application/x-www-form-urlencoded");
		    
			// init Request
			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_ACCESSTOKEN));
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

			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_RefreshingApi, start, status, postMsg, status + "");
			return (ObjectNode)(new ObjectMapper()).readTree(result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_RefreshingApi, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_RefreshingApi_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callRefreshingAPI(start, client_id, client_secret, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
}
