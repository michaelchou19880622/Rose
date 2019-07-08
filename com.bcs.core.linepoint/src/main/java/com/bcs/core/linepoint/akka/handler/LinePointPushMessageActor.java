package com.bcs.core.linepoint.akka.handler;

import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.jcodec.common.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.api.model.LinePointResponseModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.repository.LinePointMainRepository;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.RestfulUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.UntypedActor;

public class LinePointPushMessageActor extends UntypedActor {

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof LinePointPushModel) {
			// get bean
			LinePointDetailService linePointDetailService = ApplicationContextProvider.getApplicationContext().getBean(LinePointDetailService.class);
			
			// get push data
			LinePointPushModel pushApiModel = (LinePointPushModel) object;
			JSONArray uids = pushApiModel.getUid();
			
			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			String accessToken = CoreConfigReader.getString("LinePoint", CONFIG_STR.ChannelToken.toString(), true); // LinePoint.ChannelToken
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
			
			// initialize request body
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_PUSH_URL.toString(), true); // https://api.line.me/pointConnect/v1/issue
		    String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true); // 10052
		    requestBody.put("clientId", clientId);
			requestBody.put("amount", pushApiModel.getAmount());
			
			for(Integer i = 0; i < uids.length(); i++) {

				// initialize detail
				LinePointDetail detail = new LinePointDetail();
				//detail.setLinePointMainId(eventId);
				detail.setAmount(pushApiModel.getAmount());
				detail.setTriggerTime(pushApiModel.getTriggerTime());
				//detail.setSource(pushApiModel.getSource());
				
				
				// memberId
				requestBody.put("memberId", uids.get(i));
				
				// orderKey
				MessageDigest salt = MessageDigest.getInstance("SHA-256");
				String hashStr = "" + uids.get(i) + (new Date()).getTime() + pushApiModel.getEventId();
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
//					Logger.info(Id);
//					Logger.info(Time.toString());					
//					Logger.info(Type);					
//					Logger.info(Amount.toString());					
//					Logger.info(Balance.toString());

					detail.setTranscationId(Id);
					detail.setTranscationTime(Time);
					detail.setTranscationType(Type);
					detail.setTransactionAmount(Amount);
					//detail.setTransactionBalance(Balance);
					//detail.setDescription("");
					detail.setStatus(LinePointDetail.STATUS_SUCCESS);
				} catch (HttpClientErrorException e) {
					//detail.setDescription(e.getResponseBodyAsString());
					detail.setStatus(LinePointDetail.STATUS_FAIL);
				}
				
				detail.setUid(uids.get(i).toString());
				detail.setOrderKey(orderKey);
				detail.setApplicationTime(applicationTime);
				detail.setSendTime(new Date());

				Logger.info("detail1: " + detail.toString());
				//linePointDetailService.save(detail);
			}
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