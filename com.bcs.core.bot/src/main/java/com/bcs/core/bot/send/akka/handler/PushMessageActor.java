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

	/* 因為欄位設定(nvarchar 1024)，暫定最多只取1000長度的data。 */
    public static final int MAX_DATA_LENGTH = 1000;
	
	@Override
	public void onReceive(Object object) throws Exception {
		log.info("---------------- PushMessageActor ----------------");
		if(object instanceof PushApiModel) {
			PushApiModel pushApiModel = (PushApiModel) object;
			log.info("PushApiModel => {}", pushApiModel);
			
			log.info("pushApiModel.getSendTimeType() = {}", pushApiModel.getSendTimeType());
			if (pushApiModel.getSendTimeType().equals(PushApiModel.SEND_TYPE_IMMEDIATE)) { // 立即發送
				String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
				log.info("URL => {}", url);
				
				String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
				log.info("accessToken => {}", accessToken);
				
//				String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
//				log.info("serviceCode => {}", serviceCode);
				
				JSONObject requestBody = new JSONObject();

				/* 設定 request headers */
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
//				headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
				log.info("Headers => {}", headers);

				requestBody.put("messages", pushApiModel.getMessages());
				log.info("1-1 requestBody => {}", requestBody.toString());

				JSONArray uids = pushApiModel.getUid();
				log.info("Uids => {}", uids);
				
				for(Integer i = 0; i < uids.length(); i++) {
					PushMessageRecord record = new PushMessageRecord();

					requestBody.put("to", uids.get(i));
					log.info("1-2 requestBody => {}", requestBody.toString());

					/* 將 headers 跟 body 塞進 HttpEntity 中  */
					HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);

					RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

					try {
						JSONObject jsonObjOfRestfulExcuted = restfulUtil.execute();
						log.info("jsonObjOfRestfulExcuted => {}", jsonObjOfRestfulExcuted.toString());
						
						String sendMessage = pushApiModel.getMessages().toString();
						log.info("sendMessage => {}", sendMessage);
						log.info("sendMessage Length => {}", sendMessage.length());
						
						if (sendMessage.length() >= MAX_DATA_LENGTH) {  // 因為DB欄位設定的關係(nvarchar 255)，暫時修改最多只取200長度的data。
							sendMessage = sendMessage.substring(0, MAX_DATA_LENGTH);
							log.info("substring sendMessage = {}", sendMessage);
							log.info("substring sendMessage.length() = {}", sendMessage.length());
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
						
						if (strErrorMessage.length() >= MAX_DATA_LENGTH) {
							strErrorMessage = strErrorMessage.substring(0, MAX_DATA_LENGTH);
							log.info("substring strErrorMessage = {}", strErrorMessage);
							log.info("substring strErrorMessage.length() = {}", strErrorMessage.length());
						}
						
						String sendMessage = pushApiModel.getMessages().toString();
						log.info("sendMessage = {}", sendMessage);
						log.info("sendMessage.length() = {}", sendMessage.length());
						
						if (sendMessage.length() >= MAX_DATA_LENGTH) {
							sendMessage = sendMessage.substring(0, MAX_DATA_LENGTH);
							log.info("substring sendMessage = {}", sendMessage);
							log.info("substring sendMessage.length() = {}", sendMessage.length());
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
								
								String detailErrorMessage = errorMessage.getJSONArray("details").toString();
								log.info("detailErrorMessage => {}", detailErrorMessage);
								log.info("detailErrorMessage Length => {}", detailErrorMessage.length());
								
								if (detailErrorMessage.length() >= MAX_DATA_LENGTH) {
									detailErrorMessage = detailErrorMessage.substring(0, MAX_DATA_LENGTH);
									log.info("substring detailErrorMessage = {}", detailErrorMessage);
									log.info("substring detailErrorMessage.length() = {}", detailErrorMessage.length());
								}
								
								record.setDetailMessage(detailErrorMessage);
							} else {
								record.setDetailMessage(strErrorMessage);
							}
						} 
						
					} catch (Exception e) {
						log.info("Exception = {}", e);
						
						String exceptionErrorMessage = e.getMessage();
						log.info("exceptionErrorMessage = {}", exceptionErrorMessage);
						log.info("exceptionErrorMessage.length() = {}", exceptionErrorMessage.length());
						
						if (exceptionErrorMessage.length() >= MAX_DATA_LENGTH) {
							exceptionErrorMessage = exceptionErrorMessage.substring(0, MAX_DATA_LENGTH);
							log.info("substring exceptionErrorMessage = {}", exceptionErrorMessage);
							log.info("substring exceptionErrorMessage.length() = {}", exceptionErrorMessage.length());
						}
						
						String sendMessage = pushApiModel.getMessages().toString();
						log.info("sendMessage = {}", sendMessage);
						log.info("sendMessage.length() = {}", sendMessage.length());
						
						if (sendMessage.length() >= MAX_DATA_LENGTH) {
							sendMessage = sendMessage.substring(0, MAX_DATA_LENGTH);
							log.info("substring sendMessage = {}", sendMessage);
							log.info("substring sendMessage.length() = {}", sendMessage.length());
						}
						
						record.setDepartment(pushApiModel.getDepartment());
						record.setSendTime(new Date());
						record.setServiceName(pushApiModel.getServiceName());
						record.setPushTheme(pushApiModel.getPushTheme());
						record.setUID(uids.get(i).toString());
						record.setSendMessage(sendMessage);
						record.setMainMessage("Error");
						record.setSendType(pushApiModel.getSendTimeType());
						record.setCreateTime(pushApiModel.getTriggerTime());
						record.setDetailMessage(exceptionErrorMessage);
					}

					this.getSender().tell(record, this.getSelf());
				}
			} else { // 預約發送
				log.info("pushApiModel = {}", pushApiModel);
				PushMessageTaskService pushMessageTaskService = ApplicationContextProvider.getApplicationContext().getBean(PushMessageTaskService.class);

				pushMessageTaskService.startTask(pushApiModel);
			}
		}
	}
}