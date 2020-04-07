package com.bcs.core.bot.scheduler.handler;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.api.service.model.PushApiModel;
import com.bcs.core.bot.akka.service.PNPService;
import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.RestfulUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushMessageTask implements Job {
	PNPService PNPService = ApplicationContextProvider.getApplicationContext().getBean(PNPService.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("PushMessageTask-PNPService");

		log.info("PushMessageTask-PNPService : context = {}", context);
		
		String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
		log.info("PushMessageTask-PNPService : url = {}", url);
		
		String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
		log.info("PushMessageTask-PNPService : accessToken = {}", accessToken);
		
//		String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
		PushApiModel pushApiModel = null;
		RestfulUtil restfulUtil = null;
		JSONObject requestBody = new JSONObject();
		PushMessageRecord record = null;

		/* 設定 request headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
//		headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
		log.info("PushMessageTask-PNPService : headers = {}", headers);

		try {
			pushApiModel = (PushApiModel) context.getScheduler().getContext().get("PushApiModel");
		} catch (SchedulerException e) {
//			e.printStackTrace();
			log.info("PushMessageTask-PNPService : SchedulerException = {}", e);
		}
		log.info("PushMessageTask-PNPService : pushApiModel = {}", pushApiModel);

		requestBody.put("messages", pushApiModel.getMessages());

		JSONArray uids = pushApiModel.getUid();
		for(Integer i = 0; i < uids.length(); i++) {
			record = new PushMessageRecord();

			requestBody.put("to", uids.get(i));

			/* 將 headers 跟 body 塞進 HttpEntity 中  */
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);

			try {
				restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);

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
			} catch (KeyManagementException | NoSuchAlgorithmException e1) {
				e1.printStackTrace();
				log.info("PushMessageTask-PNPService : KeyManagementException | NoSuchAlgorithmException = {}", e1);
			} catch (Exception e) {
				log.info("PushMessageTask-PNPService : Exception = {}", e);
				if(e instanceof HttpClientErrorException) {
					HttpClientErrorException exception = (HttpClientErrorException) e;
					JSONObject errorMessage = new JSONObject(exception.getResponseBodyAsString());

					if(errorMessage.has("message")) {
						record.setDepartment(pushApiModel.getDepartment());
						record.setUID(uids.get(i).toString());
						record.setSendMessage(pushApiModel.getMessages().toString());
						record.setStatusCode(exception.getStatusCode().toString());
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
			}
			PNPService.tell(record);
		}
	}
}