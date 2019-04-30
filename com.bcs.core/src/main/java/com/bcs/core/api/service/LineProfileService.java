package com.bcs.core.api.service;

import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;
import com.bcs.core.utils.RestfulUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class LineProfileService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LineProfileService.class);

	public ObjectNode callGetProfileAPI(String access_token) throws Exception{
		Date start = new Date();
		return this.callGetProfileAPI(start, access_token, 0);
	}

	public ObjectNode callGetProfileAPI(Date start, String access_token, int retryCount) throws Exception{
		logger.debug("callGetProfileAPI");

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
			
			String uri = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_PROFILE);
			
			HttpGet requestGet = new HttpGet(uri);
			logger.debug("URI : " + requestGet.getURI());
			
			requestGet.addHeader("Authorization", "Bearer " + access_token);
			logger.debug("Authorization : Bearer " + access_token);
			
			// execute Call
			HttpResponse clientResponse = httpClient.execute(requestGet);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestGet.releaseConnection();

			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ProfileApi, start, status, result, status + "");
			return (ObjectNode)(new ObjectMapper()).readTree(result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ProfileApi, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ProfileApi_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callGetProfileAPI(start, access_token, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	
	public JSONObject getUserProfile(String UID) throws Exception {
		String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), "ChannelToken", true);
		String url = CoreConfigReader.getString(CONFIG_STR.LINE_GET_PROFILE_URL.toString());		
		url = UriComponentsBuilder.fromUriString(url).path(UID).build().toUriString();
		
		/* 設定 request headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		HttpEntity<?> httpEntity = new HttpEntity<>(headers);
		
		RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.GET, url, httpEntity);

		return restfulUtil.execute();
	}
	
	public String getUserNickName(String UID) throws Exception {
		return this.getUserProfile(UID).getString("displayName");
	}
}
