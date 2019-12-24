package com.bcs.core.linepoint.akka.handler;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.bot.scheduler.handler.ExecuteSendMsgTask;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.exception.BcsNoticeException;
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
			LinePointMainService linePointMainService = ApplicationContextProvider.getApplicationContext().getBean(LinePointMainService.class);
			MsgMainService msgMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgMainService.class);
			MsgSendMainService msgSendMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class);
			MsgDetailService msgDetailService = ApplicationContextProvider.getApplicationContext().getBean(MsgDetailService.class);
			// get push data
			LinePointPushModel pushApiModel = (LinePointPushModel) object;
			JSONArray detailIds = pushApiModel.getDetailIds();
			
			// get line point main
			LinePointMain linePointMain = linePointMainService.findOne(pushApiModel.getEventId());
			
			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true); // TaishinBank.ChannelToken
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
			
			// initialize request body
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_PUSH_URL.toString(), true); // https://api.line.me/pointConnect/v1/issue
		    String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true); // 10052
		    requestBody.put("clientId", clientId);
			
			
			for(Integer i = 0; i < detailIds.length(); i++) {
				// detailId
				String detailIdStr = "" + detailIds.get(i);
				Long detailId = Long.parseLong(detailIdStr);
				
				// initialize detail
				LinePointDetail detail = linePointDetailService.findOne(detailId);
				detail.setTriggerTime(pushApiModel.getTriggerTime());
				detail.setDetailType(LinePointDetail.DETAIL_TYPE_ISSUE_BCS);
				requestBody.put("amount", detail.getAmount());
				
				// memberId
				requestBody.put("memberId", detail.getUid());
				
				// orderKey
				MessageDigest salt = MessageDigest.getInstance("SHA-256");
				String hashStr = "" + detail.getUid() + (new Date()).getTime() + pushApiModel.getEventId();
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
					detail.setBalance(Balance);
					detail.setMessage("");
					detail.setStatus(LinePointDetail.STATUS_SUCCESS);
					
					linePointMain.setSuccessfulAmount(linePointMain.getSuccessfulAmount() + Amount);
					linePointMain.setSuccessfulCount(linePointMain.getSuccessfulCount() + 1);
				} catch (HttpClientErrorException e) {
					detail.setMessage(e.getResponseBodyAsString());
					detail.setStatus(LinePointDetail.STATUS_FAIL);
					
					linePointMain.setFailedCount(linePointMain.getFailedCount() + 1);
				}
				
				
				
				detail.setOrderKey(orderKey);
				detail.setApplicationTime(applicationTime);
				detail.setSendTime(new Date());

				Logger.info("detail1: " + detail.toString());
				linePointDetailService.save(detail);
				linePointMainService.save(linePointMain);
				
				if(detail.getStatus().equals(LinePointDetail.STATUS_SUCCESS)) {
					Long msgId = linePointMain.getAppendMessageId();
					if(StringUtils.isNotEmpty(msgId.toString())) {
						Logger.info("LinePoint發送成功 開始發訊息給會員");
						MsgMain msgMain = msgMainService.findOne(msgId);
						Logger.info("executeSendMsg : msgMain = " + msgMain);
						List<String> mids = new ArrayList<String>();
						mids.add(detail.getUid());
						
						if(msgMain != null){
							MsgSendMain msgSendMain = msgSendMainService.copyFromMsgMain(msgId, new Long(mids.size()), "LPSG;"+linePointMain.getTitle());
							List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);
							ExecuteSendMsgTask runTask = new ExecuteSendMsgTask();
							runTask.sendMsgToMids(mids, details, msgSendMain.getMsgSendId());
							Logger.info(" LinePoint訊息發出 ");
						}
					}
				}
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