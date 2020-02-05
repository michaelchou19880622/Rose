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

public class PushMessageActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof PushApiModel) {
			PushApiModel pushApiModel = (PushApiModel) object;

			if(pushApiModel.getSendTimeType().equals(PushApiModel.SEND_TYPE_IMMEDIATE)) {	// 立即發送
				String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
				String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
				String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
				JSONObject requestBody = new JSONObject();
				PushMessageRecord record = null;

				/* 設定 request headers */
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
				headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);

				requestBody.put("messages", pushApiModel.getMessages());

				JSONArray uids = pushApiModel.getUid();
				for(Integer i = 0; i < uids.length(); i++) {
					record = new PushMessageRecord();

					requestBody.put("to", uids.get(i));

					/* 將 headers 跟 body 塞進 HttpEntity 中  */
					HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);

					RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

					try {
						restfulUtil.execute();

						record.setDepartment(pushApiModel.getDepartment());
						record.setUID(uids.get(i).toString());
						record.setSendMessage(pushApiModel.getMessages().toString());
						record.setStatusCode(HttpStatus.OK.toString());
						record.setMainMessage("Success");
						record.setSendType(pushApiModel.getSendTimeType());
						record.setSendTime(new Date());
						record.setCreateTime(pushApiModel.getTriggerTime());
						record.setServiceName(pushApiModel.getServiceName());
						record.setPushTheme(pushApiModel.getPushTheme());
					} catch (HttpClientErrorException e) {
						JSONObject errorMessage = new JSONObject(e.getResponseBodyAsString());

						if(errorMessage.has("message")) {
							record.setDepartment(pushApiModel.getDepartment());
							record.setUID(uids.get(i).toString());
							record.setSendMessage(pushApiModel.getMessages().toString());
							record.setStatusCode(e.getStatusCode().toString());
							record.setMainMessage(errorMessage.getString("message"));
							record.setSendType(pushApiModel.getSendTimeType());
							record.setSendTime(new Date());
							record.setCreateTime(pushApiModel.getTriggerTime());
							record.setServiceName(pushApiModel.getServiceName());
							record.setPushTheme(pushApiModel.getPushTheme());
							if(errorMessage.has("details"))
								record.setDetailMessage(errorMessage.getJSONArray("details").toString());
						}
					}
					this.getSender().tell(record, this.getSelf());
				}
			} else {	// 預約發送
				PushMessageTaskService pushMessageTaskService = ApplicationContextProvider.getApplicationContext().getBean(PushMessageTaskService.class);

				pushMessageTaskService.startTask(pushApiModel);
			}
		}
	}
}