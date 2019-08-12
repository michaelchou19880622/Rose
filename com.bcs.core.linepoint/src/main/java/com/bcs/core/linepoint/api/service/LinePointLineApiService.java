package com.bcs.core.linepoint.api.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bcs.core.api.service.model.PostLineResponse;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;

@Service
public class LinePointLineApiService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointLineApiService.class);
	/*
	public PostLineResponse callCreateRichMenuAPI(String channelId, RichMenu richMenu, int retryCount)
			throws Exception {
		logger.debug("callCreateRichMenuAPI");

		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_CREATE_API);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
			
			ObjectMapper mapper = new ObjectMapper();
			String dataStr = mapper.writeValueAsString(richMenu);
		    StringEntity entity = new StringEntity(dataStr, "UTF-8");
		    entity.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
		    
			// init Request
			HttpPost requestPost = new HttpPost(apiUrl);
			logger.debug("URI : " + requestPost.getURI());
			requestPost.setEntity(entity);
			requestPost.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestPost.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_CreateRichMenu, start, status, richMenu.toString(), status + "", true);
			return new PostLineResponse(status, result);
		}catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_CreateRichMenu_Error, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_CreateRichMenu_Error, start, status, richMenu.toString(), status + "");
			
			if(retryCount < 5){
				return this.callCreateRichMenuAPI(channelId, richMenu, retryCount + 1);
			}else{
				throw e;
			}
		}
	}
	
	public PostLineResponse callUploadImageAPI(String channelId, String richMenuId, String imageType, String resourceId, int retryCount)
			throws Exception {
		logger.debug("callUploadImageRichMenuAPI");

		Map<String, String> map = new HashMap<String, String>();
		map.put("richMenuId", richMenuId);
		map.put("imageType", imageType);
		map.put("resourceId", resourceId);
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_UPLOAD_IMAGE_API);
			apiUrl = apiUrl.replace("{richMenuId}", richMenuId);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
			
			String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + "IMAGE";
			File image = new File(filePath + System.getProperty("file.separator") + resourceId);
			
			HttpEntity entity = new FileEntity(image);
		    
			// init Request
			HttpPost requestPost = new HttpPost(apiUrl);
			logger.debug("URI : " + requestPost.getURI());
			requestPost.setEntity(entity);
			requestPost.addHeader("Authorization", "Bearer " + channelAccessToken);
			requestPost.addHeader("Content-Type", imageType);
//			requestPost.addHeader("Content-Length", String.valueOf(entity.getContentLength()));
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestPost.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_UploadImageToRichMenu, start, status, map.toString(), richMenuId + "-" + status, true);
			return new PostLineResponse(status, result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_UploadImageToRichMenu_Error, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_UploadImageToRichMenu_Error, start, status, map.toString(), status + "");
			
			if(retryCount < 5){
				return this.callUploadImageAPI(channelId, richMenuId, imageType, resourceId, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	// Set UID's Rich Menu
	public PostLineResponse callLinkRichMenuToUserAPI(String richMenuId, String uid, int retryCount) throws Exception {
		logger.debug("callLinkRichMenuToUserAPI");

		Map<String, String> map = new HashMap<String, String>();
		map.put("richMenuId", richMenuId);
		map.put("uid", uid);
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_LINK_API);
			apiUrl = apiUrl.replace("{richMenuId}", richMenuId);
			apiUrl = apiUrl.replace("{userId}", uid);
			String channelAccessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpPost requestPost = new HttpPost(apiUrl);
			logger.debug("URI : " + requestPost.getURI());
			requestPost.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestPost.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_LinkRichMenuToUser, start, status, map.toString(), status + "");
			return new PostLineResponse(status, result);
		}catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_LinkRichMenuToUser, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_LinkRichMenuToUser_Error, start, status, map.toString(), status + "");
			
			if(retryCount < 5)
				return this.callLinkRichMenuToUserAPI(richMenuId, uid, retryCount + 1);
			else
				throw e;
		}
	}
	
	// Set Default Rich Menu
	public PostLineResponse callLinkRichMenuToAllUserAPI(String richMenuId, int retryCount) throws Exception {
		logger.info("callLinkRichMenuToAllUserAPI");

		// error log
		Map<String, String> map = new HashMap<String, String>();
		map.put("richMenuId", richMenuId);
		Date start = new Date();
		int status = 0;
		
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_LINK_ALL_API);
			apiUrl = apiUrl.replace("{richMenuId}", richMenuId);
			String channelAccessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// Initialize Request
			HttpPost requestPost = new HttpPost(apiUrl);
			logger.info("URI : " + requestPost.getURI());
			requestPost.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// Execute Call
			HttpResponse clientResponse = httpclient.execute(requestPost);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.info("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.info("clientResponse result : " + result);
			
			requestPost.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_LinkRichMenuToUser, start, status, map.toString(), status + "");
			return new PostLineResponse(status, result);
		}catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_LinkRichMenuToUser, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_LinkRichMenuToUser_Error, start, status, map.toString(), status + "");
			if(retryCount < 5)
				return this.callLinkRichMenuToAllUserAPI(richMenuId, retryCount + 1);
			else
				throw e;
		}
	}
	
	
	public PostLineResponse callUnlinkRichMenuToUserAPI(String channelId, String uid, int retryCount)
			throws Exception {
		logger.debug("callUnlinkRichMenuToUserAPI");
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_UNLINK_API);
			apiUrl = apiUrl.replace("{userId}", uid);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpDelete requestDelete = new HttpDelete(apiUrl);
			logger.debug("URI : " + requestDelete.getURI());
			requestDelete.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestDelete);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestDelete.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_UnlinkRichMenuToUser, start, status, uid, status + "");
			return new PostLineResponse(status, result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_UnlinkRichMenuToUser, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_UnlinkRichMenuToUser_Error, start, status, uid, status + "");
			
			if(retryCount < 5){
				return this.callUnlinkRichMenuToUserAPI(channelId, uid, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	
	public PostLineResponse callDeleteRichMenuAPI(String channelId, String richMenuId, int retryCount)
			throws Exception {
		logger.debug("callDeleteRichMenuAPI");
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_DELETE_API);
			apiUrl = apiUrl.replace("{richMenuId}", richMenuId);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpDelete requestDelete = new HttpDelete(apiUrl);
			logger.debug("URI : " + requestDelete.getURI());
			requestDelete.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestDelete);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestDelete.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_DeleteRichMenu, start, status, richMenuId, status + "", true);
			return new PostLineResponse(status, result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_DeleteRichMenu, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_DeleteRichMenu_Error, start, status, richMenuId, status + "");
			
			if(retryCount < 5){
				return this.callDeleteRichMenuAPI(channelId, richMenuId, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	
	public PostLineResponse callGetRichMenuInfoAPI(String channelId, String richMenuId, int retryCount)
			throws Exception {
		logger.debug("callGetRichMenuInfoAPI");
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_GET_INFO_API);
			apiUrl = apiUrl.replace("{richMenuId}", richMenuId);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpGet requestGet = new HttpGet(apiUrl);
			logger.debug("URI : " + requestGet.getURI());
			requestGet.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestGet);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestGet.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuInfo, start, status, richMenuId, status + "", true);
			return new PostLineResponse(status, result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuInfo, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuInfo_Error, start, status, richMenuId, status + "");
			
			if(retryCount < 5){
				return this.callGetRichMenuInfoAPI(channelId, richMenuId, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	
	public PostLineResponse callGetRichMenuInfoListAPI(String channelId, int retryCount)
			throws Exception {
		logger.debug("callGetRichMenuInfoListAPI");
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_GET_INFO_LIST_API);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpGet requestGet = new HttpGet(apiUrl);
			logger.debug("URI : " + requestGet.getURI());
			requestGet.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestGet);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestGet.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuInfoList, start, status, "", status + "", true);
			return new PostLineResponse(status, result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuInfoList, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuInfoList_Error, start, status, "", status + "");
			
			if(retryCount < 5){
				return this.callGetRichMenuInfoListAPI(channelId, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	
	public PostLineResponse callGetRichMenuIdOfUserAPI(String channelId, String uid, int retryCount)
			throws Exception {
		logger.debug("callGetRichMenuIdOfUserAPI");
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_GET_LINK_ID_OF_USER_API);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpGet requestGet = new HttpGet(apiUrl);
			logger.debug("URI : " + requestGet.getURI());
			requestGet.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestGet);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.debug("clientResponse result : " + result);
			
			requestGet.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuIdOfUser, start, status, "", status + "");
			return new PostLineResponse(status, result);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuIdOfUser, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_GetRichMenuIdOfUser_Error, start, status, "", status + "");
			
			if(retryCount < 5){
				return this.callGetRichMenuIdOfUserAPI(channelId, uid, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	
	public PostLineResponse callDownloadRichMenuImageAPI(String channelId, String richMenuId, int retryCount)
			throws Exception {
		logger.debug("callDownloadRichMenuImageAPI");
		
		Date start = new Date();
		int status = 0;
		try{
			String apiUrl = CoreConfigReader.getString(CONFIG_STR.LINE_RICH_MENU_DOWNLOAD_IMAGE_API);
			apiUrl = apiUrl.replace("{richMenuId}", richMenuId);
			String channelAccessToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
			
			HttpClient httpclient = HttpClientUtil.generateClient();
		    
			// init Request
			HttpGet requestGet = new HttpGet(apiUrl);
			logger.debug("URI : " + requestGet.getURI());
			requestGet.addHeader("Authorization", "Bearer " + channelAccessToken);
	
			// execute Call
			HttpResponse clientResponse = httpclient.execute(requestGet);
			
			status = clientResponse.getStatusLine().getStatusCode();
			logger.debug("clientResponse StatusCode : " + status);
	
			InputStream is = null;
			BufferedImage image = null;
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				
				is = clientResponse.getEntity().getContent();
				image = ImageIO.read(is);
				
			}
			logger.debug("clientResponse result : " + is);
			
			String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + "RICHMENUIMAGE";
			
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			String imageId = UUID.randomUUID().toString().toLowerCase();
			File genfile = new File(filePath + System.getProperty("file.separator") + imageId);
			if(!genfile.exists()){
				genfile.createNewFile();
			}		

			ImageIO.write(image, "jpg", genfile);
			
			requestGet.releaseConnection();
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_DownloadRichMenuImage, start, status, "", status + "", true);
			return new PostLineResponse(status, imageId);
		}
		catch(Exception e){
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_DownloadRichMenuImage, error, e.getMessage());
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichMenuApi, LOG_TARGET_ACTION_TYPE.ACTION_DownloadRichMenuImage_Error, start, status, "", status + "");
			
			if(retryCount < 5){
				return this.callDownloadRichMenuImageAPI(channelId, richMenuId, retryCount + 1);
			}
			else{
				throw e;
			}
		}
	}
	*/
}
