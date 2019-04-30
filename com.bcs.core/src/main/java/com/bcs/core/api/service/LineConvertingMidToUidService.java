package com.bcs.core.api.service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class LineConvertingMidToUidService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LineConvertingMidToUidService.class);

	public ObjectNode callGetConvertingAPI(String access_token, String MID) throws Exception{
		Date start = new Date();
		return this.callGetConvertingAPI(start, access_token, MID, 0);
	}

	public ObjectNode callGetConvertingAPI(Date start, String access_token, String MID, int retryCount) throws Exception{
		logger.info("callGetConvertingAPI");

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
			
			String uri = CoreConfigReader.getString(CONFIG_STR.LINE_CONVERTING_GET) + "?mid=" + MID;
			
			HttpGet requestGet = new HttpGet(uri);
			logger.info("URI : " + requestGet.getURI());
			
			requestGet.addHeader("Authorization", "Bearer " + access_token);
			logger.debug("Authorization : Bearer " + access_token);
			
			// execute Call
			HttpResponse clientResponse = httpClient.execute(requestGet);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.info("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestGet.releaseConnection();

			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetConvertingAPI, start, status, "mid=" + MID, status + "");
			if(status == 200){
				ObjectNode obj = (new ObjectMapper()).createObjectNode();
				obj.put("UID", result);
				return obj;
			}
			else{
				ObjectNode obj = (new ObjectMapper()).createObjectNode();
				obj.put("convertResult", result);
				return obj;
			}
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetConvertingAPI, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetConvertingAPI_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callGetConvertingAPI(start, access_token, MID, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}

	public ObjectNode callPostConvertingAPI(String access_token, List<String> MIDs) throws Exception{
		Date start = new Date();
		return this.callPostConvertingAPI(start, access_token, MIDs, 0);
	}

	public ObjectNode callPostConvertingAPI(Date start, String access_token, List<String> MIDs, int retryCount) throws Exception{
		return this.callPostConvertingAPI(start, access_token, MIDs, false, null, retryCount);
	}

	public ObjectNode callPostConvertingAPI(String access_token, List<String> MIDs, boolean saveFile, String filePath) throws Exception{
		Date start = new Date();
		return this.callPostConvertingAPI(start, access_token, MIDs, saveFile, filePath, 0);
	}

	public ObjectNode callPostConvertingAPI(Date start, String access_token, List<String> MIDs, boolean saveFile, String filePath, int retryCount) throws Exception{
		logger.info("callPostConvertingAPI");

		int status = 0;
		try{
			HttpClient httpClient = HttpClientUtil.generateClient();
			
			String postMsg = StringUtils.join(MIDs.toArray(), "\n");
			
		    StringEntity entity = new StringEntity(postMsg, "UTF-8");
		    entity.setContentType(MediaType.TEXT_PLAIN_VALUE);
		    
			// init Request
			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(CONFIG_STR.LINE_CONVERTING_POST));
			logger.info("URI : " + requestPost.getURI());
			requestPost.setEntity(entity);
			
			requestPost.addHeader("Authorization", "Bearer " + access_token);
			logger.debug("Authorization : Bearer " + access_token);
	
			// print requestPost
			logger.debug("MIDs:" + MIDs.size());
	
			// execute Call
			HttpResponse clientResponse = httpClient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.info("clientResponse StatusCode : " + status);
	
			if(saveFile && StringUtils.isNotBlank(filePath)){

				String errorMsg = "";
				if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
					
					try{
						InputStreamUtil.saveInputStr(clientResponse.getEntity().getContent(), filePath, true);
					}
					catch(Exception e){
						errorMsg = e.getMessage();
					}
				}

				Map<String, Object> r = new HashMap<String, Object>(); 
				r.put("filePath", filePath);
				r.put("status", status);
				r.put("errorMsg", errorMsg);
	
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_PostConvertingAPI, start, status, "MIDs:" + MIDs.size(), status + "");
				return ObjectUtil.jsonStrToObjectNode(ObjectUtil.objectToJsonStr(r));
				
			}
			else{
				String result = "";
				if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
					
					result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent(), true);
				}
				logger.debug("clientResponse result : " + result);
				
				requestPost.releaseConnection();
				
				String[] array = result.split("\\n");
				
				List<String> list = Arrays.asList(array);
	
				Map<String, Object> r = new HashMap<String, Object>(); 
				r.put("list", list);
				r.put("status", status);
	
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_PostConvertingAPI, start, status, "MIDs:" + MIDs.size(), status + "");
				return ObjectUtil.jsonStrToObjectNode(ObjectUtil.objectToJsonStr(r));
			}
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_PostConvertingAPI, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_PostConvertingAPI_Error, start, status, error, status + "");
			if(retryCount < 5){
				return this.callPostConvertingAPI(start, access_token, MIDs, saveFile, filePath, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
}
