package com.bcs.core.linepoint.api.service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.bcs.core.api.service.model.PushApiModel;
//import com.bcs.core.db.service.MsgAudienceMatchSendService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.linepoint.api.model.LinePointTaskModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
//import com.bcs.core.interactive.service.LocationInteractiveService;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.record.service.CatchHandleMsgReceiveTimeout;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.utils.RestfulUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

@Service
public class LinePointApiService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointApiService.class);
	
	public String getLinePointChannelAccessToken() {
		logger.info("[getLinePointChannelAccessToken]");
		try {
			String channelId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CHANNEL_ID.toString(), true);
			String channelSecret = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CHANNEL_SECRET.toString(), true);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = null;
			String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);    // Proxy Server 的位置

			if (StringUtils.isNotBlank(proxyUrl)) {
				logger.info("Use proxy and proxy url is: " + proxyUrl);
				clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().setProxy(new HttpHost(proxyUrl, 80, "http")).build());
			}

			/* 設定 request body */
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("grant_type", "client_credentials");
			map.add("client_id", channelId);
			map.add("client_secret", channelSecret);
			
			HttpEntity<MultiValueMap<String, String>> accessTokenEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);

			/* 以 Post 方式送出 request (如果有需要用 Proxy，便將上面設定好的 clientHttpRequestFactory 加進來) */
			RestTemplate restTemplate = (clientHttpRequestFactory == null) ? new RestTemplate() : new RestTemplate(clientHttpRequestFactory);
			ResponseEntity<String> accessTokenResponse = restTemplate.postForEntity(CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_ACCESSTOKEN), accessTokenEntity, String.class);
			
			String responseBody = accessTokenResponse.getBody(); // Response 的結果
			logger.info("responseBody:"+responseBody);
			
			JSONObject responseObj = new JSONObject(responseBody);
			String access_token = responseObj.get("access_token").toString(); // 將 id_token 從 response body 中拿出來
			logger.info("access_token:"+access_token);
			return access_token;
			/*
			String[] parsedJWT = ID_Token.split("[.]");	// 將 id_token 以逗點為基準切成 header、payload、signature 三個部分
			
			String header = parsedJWT[0], payload = parsedJWT[1], signature = parsedJWT[2];
			Base64.Decoder base64Decoder = Base64.getDecoder();
			
			// 驗證接收到的 ID Token 是否為合法的 JWT 
			if(validateJWT(ChannelSecret, header, payload, signature, ChannelID, null)) {			
				JSONObject payloadObject = new JSONObject(new String(base64Decoder.decode(payload), "UTF-8"));	// 將 payload 用 base64 解碼後轉為 UTF-8 字串，再轉換成 JSON 物件
				
				String UID = payloadObject.get("sub").toString();	// 從解析出來的 JSON 物件中取得使用者的 UID
				
				session.setAttribute("UID", UID); // 將 UID 存入 session，以便後續的刮刮卡流程使用
				
				response.sendRedirect(contentGameService.tranferURI("BcsPage:ScratchCardPage:" + state, UID));
			} else {
				throw new Exception("Illegal JWT !");
			}			
			*/
			
			/*
			HttpClient httpClient = HttpClientUtil.generateClient();
			
			List<String> list = new ArrayList<String>();
			list.add("grant_type=" + URLEncoder.encode("client_credentials", "UTF-8"));		
			list.add("channelId=" + URLEncoder.encode(channelId, "UTF-8"));		
			list.add("channelSecret=" + URLEncoder.encode(channelSecret, "UTF-8"));		
			String postMsg = StringUtils.join(list.toArray(), "&");
			
		    StringEntity entity = new StringEntity(postMsg, "UTF-8");
		    entity.setContentType("application/x-www-form-urlencoded");
		    
			// init Request
			HttpPost requestPost = new HttpPost(url);
			logger.info("URI : " + requestPost.getURI());
			requestPost.setEntity(entity);
	
			// print requestPost
			logger.info("postMsg : " + postMsg);
	
			// execute Call
			HttpResponse clientResponse = httpClient.execute(requestPost);
			
			int status = clientResponse.getStatusLine().getStatusCode();
			logger.info("clientResponse StatusCode : " + status);
	
			logger.info("getContent:"+clientResponse.getEntity().getContent());
			
			String result = "";
			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
			}
			logger.info("clientResponse result : " + result);
			*/
			/*
			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			// initialize request body
			JSONObject requestBody = new JSONObject();

		    requestBody.put("grant_type", "client_credentials");
			requestBody.put("client_id", channelId);
			requestBody.put("client_secret", channelSecret);
			
			// HttpEntity by header and body
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
			JSONObject responseObject = null;
			try {
				responseObject = restfulUtil.execute();
				
				String accessToken = responseObject.getString("access_token");
				logger.info("access_token:"+accessToken);
				return accessToken;
			} catch (HttpClientErrorException e) {
				logger.info("[getLinePointChannelAccessToken] Status code: " + e.getStatusCode());
				logger.info("[getLinePointChannelAccessToken]  Response body: " + e.getResponseBodyAsString());
				return "";
			}
			*/
		} catch(Exception e) {
			return "[getLinePointChannelAccessToken] e:"+ e.toString();
		}
	}
	
//	public ObjectNode callLinePointAPI(String clientId, String memberId, String orderKey, Long amount, String channelToken) throws Exception{
//		Date start = new Date();
//		return this.callLinePointAPI(start, clientId, memberId, orderKey, amount, channelToken, 0);
//	}
//	
//	public ObjectNode callLinePointAPI(Date start, String clientId, String memberId, String orderKey, Long amount,
//			String channelToken, int retryCount) throws Exception{
//		logger.info("LineAudienceMatchApiService callMissionStickersAPI");
//
//		int status = 0;
//		try{
//			logger.info("LineAudienceMatchApiService 1111");
//			
//			HttpClient httpClient = HttpClientUtil.generateClient();
//			logger.info("LineAudienceMatchApiService 2222");
//			// Request Body
//			String applicationTime = String.valueOf(Instant.now().toEpochMilli());
//			ObjectNode requestContent = new ObjectMapper().createObjectNode();
//			requestContent.put("clientId", clientId);
//			requestContent.put("memberId", memberId);
//			requestContent.put("orderKey", orderKey);
//			requestContent.put("applicationTime", applicationTime);
//			requestContent.put("amount", amount);
//						
//			logger.info("LineAudienceMatchApiService requestContent.toString():" + requestContent.toString());
//			
//		    StringEntity entity = new StringEntity(requestContent.toString(), "UTF-8");
//		    entity.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		    		  
//		    logger.info("LineAudienceMatchApiService setContentType");
//		    
//			// init Request
//			HttpPost requestPost = new HttpPost(CoreConfigReader.getString(""));
//			// CONFIG_STR.LINE_POINT_API
//			logger.info("LineAudienceMatchApiService URI : " + requestPost.getURI());
//			requestPost.setHeader("Authorization", "Bearer " +  channelToken);
//			requestPost.setEntity(entity);
//			logger.debug("LineAudienceMatchApiService requestPost : " + requestPost.toString());
//			
//			// print requestPost
//			logger.debug("LineAudienceMatchApiService requestContent : " + requestContent);
//	
//			// execute Call
//			HttpResponse clientResponse = httpClient.execute(requestPost);
//			
//			status = clientResponse.getStatusLine().getStatusCode();
//			logger.info("LineAudienceMatchApiService clientResponse StatusCode : " + status);
//	
//			String result = "";
//			if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){				
//				result += InputStreamUtil.getInputStr(clientResponse.getEntity().getContent());
//			}
//			logger.debug("LineAudienceMatchApiService clientResponse result : " + result);
//			
//			requestPost.releaseConnection();
//
//			// MsgAudienceMatchSendService amService =ApplicationContextProvider.getApplicationContext().getBean(MsgAudienceMatchSendService.class);
//			//update Status
//			if (status == 200) {
//				logger.debug("LineAudienceMatchApiService clientResponse Success result : " + result + "!!!");	
//				//for(String uid: uids)
//				//	amService.updateStautsAndRespCodeByPhoneAndId(msgSendId, 8, status, uid);
//			}
//			else {
//				logger.debug("LineAudienceMatchApiService clientResponse Failure result : " + result + "!!!");				
//			}
//			// SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_MissionStickersApi, start, status, requestContent.toString(), status + "");
//			return (ObjectNode)(new ObjectMapper()).readTree(result);
//		}
//		catch(Exception e){
//			String error = ErrorRecord.recordError(e, false);
//			logger.error(error);
//			// SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_MissionStickersApi, error, e.getMessage());
//			// SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_MissionStickersApi_Error, start, status, error, status + "");
//			if(retryCount < 1){
//				return this.callLinePointAPI(start, clientId, memberId, orderKey, amount, channelToken, retryCount + 1);
//			}
//			else{
//				throw e;
//			}
//		}
//	}
}
