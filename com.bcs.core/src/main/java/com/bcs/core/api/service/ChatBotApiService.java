package com.bcs.core.api.service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.bcs.core.api.service.model.LocationModel;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.RestfulUtil;

@Service
public class ChatBotApiService {
	private static Logger logger = Logger.getLogger(ChatBotApiService.class);
	
	@Autowired
	public LineProfileService lineProfileService;
	
	public JSONObject sendMessage(String UID, String question, LocationModel location ,String msgType) throws Exception {
		String url = CoreConfigReader.getString(CONFIG_STR.GATEWAY_API_URL.toString(), true);
		String gatewaySecret = CoreConfigReader.getString(CONFIG_STR.GATEWAY_API_SECRET.toString(), true);
		String channel = CoreConfigReader.getString(CONFIG_STR.GATEWAY_CHANNEL.toString(), true);
		String apiKey = CoreConfigReader.getString(CONFIG_STR.GATEWAY_API_KEY.toString(), true);
		String nickname = lineProfileService.getUserNickName(UID);
		
		logger.info("◎ 使用者「" + nickname + "」傳送了一則訊息：" + question);
		
		/* 設定 request headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		/* 設定 request body */
		JSONObject requestBody = new JSONObject();
		requestBody.put("id", UID);
		requestBody.put("q", question);
		requestBody.put("nickname", nickname);
		requestBody.put("ch", channel);
		requestBody.put("apikey", apiKey);
		requestBody.put("sig", generateSignature(gatewaySecret, channel + UID + question));
		requestBody.put("timestamp", new Date().getTime());
		requestBody.put("type", msgType);//20190126機器人應答狀況下，如使用者傳圖片，須判斷type後在qa-ajax多帶一個參數給碩網判斷
		
		if(location != null) {
			requestBody.put("lng", location.getLongitude());
			requestBody.put("lat", location.getLatitude());
			
			logger.info("◎ 經度：" + requestBody.getString("lng") + "； 緯度：" + requestBody.getString("lat"));
		} 
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
		
		RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity, CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_USE_PROXY.toString()));

		JSONObject responseObject = restfulUtil.execute();
		
		logger.info("◎ 大腦的回答結果：" + responseObject);
		
		return responseObject;
	}
	
	public JSONObject sendMessage(String UID, LocationModel location) throws Exception {
		return this.sendMessage(UID, location.getAddress(), location, "location");
	}
	
	private String generateSignature(String key, String data) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		
		String result = Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
		
		return result;
	}
}
