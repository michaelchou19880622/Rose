package com.bcs.core.linepoint.akka.handler;

import java.security.MessageDigest;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jcodec.common.logging.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.RestfulUtil;

import akka.actor.UntypedActor;

@Slf4j
public class LinePointPushApiActor extends UntypedActor {

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof LinePointDetail) {
			// get bean
			LinePointDetailService linePointDetailService = ApplicationContextProvider.getApplicationContext().getBean(LinePointDetailService.class);

			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			String accessToken = CoreConfigReader.getString("LinePoint", CONFIG_STR.CHANNEL_TOKEN.toString(), true); // LinePoint.ChannelToken
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

			// get push data
			LinePointDetail linePointDetail = (LinePointDetail) object;
			String uid = linePointDetail.getUid();

			// initialize request body
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_PUSH_URL.toString(), true); // https://api.line.me/pointConnect/v1/issue
		    String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true); // 10052
		    requestBody.put("clientId", clientId);
			requestBody.put("amount", linePointDetail.getAmount());
			requestBody.put("memberId", uid);

			// orderKey
			MessageDigest salt = MessageDigest.getInstance("SHA-256");
			String hashStr = "" + uid + (new Date()).getTime() + "334581925";
			String hash = DigestUtils.md5Hex(hashStr);
		    salt.update(hash.toString().getBytes("UTF-8"));
		    String orderKey = bytesToHex(salt.digest()).substring(0, 48);
		    requestBody.put("orderKey", orderKey);

		    // applicationTime
		    Long applicationTime = System.currentTimeMillis();
		    requestBody.put("applicationTime", applicationTime);

			// HttpEntity by header and body
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

			// set detail
			try {
				JSONObject responseObject = restfulUtil.execute();
				Logger.info("RO1:"+responseObject.toString());

				String Id = responseObject.getString("transactionId");
				Long Time = responseObject.getLong("transactionTime");
				String Type = responseObject.getString("transactionType");
				Integer Amount = responseObject.getInt("transactionAmount");
				Integer Balance = responseObject.getInt("balance");

				linePointDetail.setTranscationId(Id);
				linePointDetail.setTranscationTime(Time);
				linePointDetail.setTranscationType(Type);
				linePointDetail.setTransactionAmount(Amount);
				linePointDetail.setBalance(Balance);
				linePointDetail.setMessage("");
				linePointDetail.setStatus(LinePointDetail.STATUS_SUCCESS);
			} catch (HttpClientErrorException e) {
				log.error("HttpClientErrorException", e);
				linePointDetail.setMessage(e.getResponseBodyAsString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
			}

			linePointDetail.setUid(uid);
			linePointDetail.setOrderKey(orderKey);
			linePointDetail.setApplicationTime(applicationTime);
			linePointDetail.setSendTime(new Date());
			linePointDetailService.save(linePointDetail);
		}
	}

	private String bytesToHex(byte[] hash) {
	  StringBuffer hexString = new StringBuffer();
	  for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xFF & hash[i]);
	    if (hex.length() == 1) hexString.append('0');
	    hexString.append(hex);
	  }
	  return hexString.toString();
	}
}