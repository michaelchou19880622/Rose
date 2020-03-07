package com.bcs.core.bot.send.akka.handler;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.api.service.model.PushApiModel;
import com.bcs.core.bot.scheduler.service.PushMessageTaskService;
import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.RestfulUtil;

import akka.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushMessageActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		log.info("---------------- PushMessageActor ----------------");
		if(object instanceof PushApiModel) {
			PushApiModel pushApiModel = (PushApiModel) object;
			log.info("PushApiModel => {}", pushApiModel);
			log.info("pushApiModel.getSendTimeType() = {}", pushApiModel.getSendTimeType());

			if(pushApiModel.getSendTimeType().equals(PushApiModel.SEND_TYPE_IMMEDIATE)) {	// 立即發送
				String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
				log.info("URL => {}", url);
				
				String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
				log.info("accessToken => {}", accessToken);
				
				String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
				log.info("serviceCode => {}", serviceCode);
				
				JSONObject requestBody = new JSONObject();

				/* 設定 request headers */
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
				headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
				log.info("Headers => {}", headers);

				requestBody.put("messages", pushApiModel.getMessages());

				JSONArray uids = pushApiModel.getUid();
				log.info("Uids => {}", uids);
				for(Integer i = 0; i < uids.length(); i++) {
					PushMessageRecord record = new PushMessageRecord();

					requestBody.put("to", uids.get(i));
					log.info("requestBody => {}", requestBody.toString());

					/* 將 headers 跟 body 塞進 HttpEntity 中  */
					HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);

					RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

					try {
						JSONObject jsonObjOfRestfulExcuted = restfulUtil.execute();
						log.info("jsonObjOfRestfulExcuted => {}", jsonObjOfRestfulExcuted);
						
						String sendMessage = pushApiModel.getMessages().toString();
						log.info("sendMessage => {}", sendMessage);
						log.info("sendMessage Length => {}", sendMessage.length());
						
						if (sendMessage.length() >= 200) {  // 因為DB欄位設定的關係(nvarchar 255)，暫時修改最多只取200長度的data。
							sendMessage = sendMessage.substring(0, 200);
						}

						record.setDepartment(pushApiModel.getDepartment());
						record.setUID(uids.get(i).toString());
						record.setSendMessage(sendMessage);
						record.setStatusCode(HttpStatus.OK.toString());
						record.setMainMessage("Success");
						record.setSendType(pushApiModel.getSendTimeType());
						record.setSendTime(new Date());
						record.setCreateTime(pushApiModel.getTriggerTime());
						record.setServiceName(pushApiModel.getServiceName());
						record.setPushTheme(pushApiModel.getPushTheme());
					} catch (HttpClientErrorException e) {
						log.info("HttpClientErrorException => {}", e);
						JSONObject errorMessage = new JSONObject(e.getResponseBodyAsString());
						
						String strErrorMessage = errorMessage.getString("message");
						log.info("strErrorMessage => {}", strErrorMessage);
						log.info("strErrorMessage Length => {}", strErrorMessage.length());
						
						if (strErrorMessage.length() >= 200) {  // 因為DB欄位設定的關係(nvarchar 255)，暫時修改最多只取200長度的data。
							strErrorMessage = strErrorMessage.substring(0, 200);
						}
						
						String sendMessage = pushApiModel.getMessages().toString();
						log.info("sendMessage = {}", sendMessage);
						log.info("sendMessage.length() = {}", sendMessage.length());
						
						if (sendMessage.length() >= 200) {  // 因為DB欄位設定的關係(nvarchar 255)，暫時修改最多只取200長度的data。
							sendMessage = sendMessage.substring(0, 200);
						}

						if(errorMessage.has("message")) {
							record.setDepartment(pushApiModel.getDepartment());
							record.setUID(uids.get(i).toString());
							record.setSendMessage(sendMessage);
							record.setStatusCode(e.getStatusCode().toString());
							record.setMainMessage("Fail");
							record.setSendType(pushApiModel.getSendTimeType());
							record.setSendTime(new Date());
							record.setCreateTime(pushApiModel.getTriggerTime());
							record.setServiceName(pushApiModel.getServiceName());
							record.setPushTheme(pushApiModel.getPushTheme());
							
							if (errorMessage.has("details")) {
								record.setDetailMessage(errorMessage.getJSONArray("details").toString());
							} else {
								record.setDetailMessage(strErrorMessage);
							}
						} 
					} catch (Exception e) {
						log.info("Exception = {}", e);
						
						String exceptionErrorMessage = e.getMessage();
						log.info("exceptionErrorMessage = {}", exceptionErrorMessage);
						log.info("exceptionErrorMessage.length() = {}", exceptionErrorMessage.length());
						
						if (exceptionErrorMessage.length() >= 200) {  // 因為DB欄位設定的關係(nvarchar 255)，暫時修改最多只取200長度的data。
							exceptionErrorMessage = exceptionErrorMessage.substring(0, 200);
						}
						
						String sendMessage = pushApiModel.getMessages().toString();
						log.info("sendMessage = {}", sendMessage);
						log.info("sendMessage.length() = {}", sendMessage.length());
						
						if (sendMessage.length() >= 200) {  // 因為DB欄位設定的關係(nvarchar 255)，暫時修改最多只取200長度的data。
							sendMessage = sendMessage.substring(0, 200);
						}
						
						record.setDepartment(pushApiModel.getDepartment());
						record.setSendTime(new Date());
						record.setServiceName(pushApiModel.getServiceName());
						record.setPushTheme(pushApiModel.getPushTheme());
						record.setUID(uids.get(i).toString());
						record.setSendMessage(sendMessage);
						record.setMainMessage(exceptionErrorMessage);
						record.setSendType(pushApiModel.getSendTimeType());
						record.setCreateTime(pushApiModel.getTriggerTime());
						record.setDetailMessage(exceptionErrorMessage);
					}
					this.getSender().tell(record, this.getSelf());
				}
			} else {	// 預約發送
				log.info("pushApiModel = {}", pushApiModel);
				PushMessageTaskService pushMessageTaskService = ApplicationContextProvider.getApplicationContext().getBean(PushMessageTaskService.class);

				pushMessageTaskService.startTask(pushApiModel);
			}
		}
	}
}