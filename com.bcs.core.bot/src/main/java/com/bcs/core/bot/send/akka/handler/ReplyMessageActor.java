package com.bcs.core.bot.send.akka.handler;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.RestfulUtil;

import akka.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReplyMessageActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {
		log.info("ReplyMessageActor-onReceive");
		log.info("ReplyMessageActor-onReceive : message = {}", message);
		
		if(message instanceof JSONObject) {
			String accessToken = CoreConfigReader.getString(CONFIG_STR.DEFAULT.toString(), CONFIG_STR.CHANNEL_TOKEN.toString(), true);
			String serviceCode = CoreConfigReader.getString(CONFIG_STR.AUTO_REPLY.toString(), CONFIG_STR.CHANNEL_SERVICE_CODE.toString(), true);
			log.info("ReplyMessageActor-onReceive : accessToken = {}", accessToken);
			log.info("ReplyMessageActor-onReceive : serviceCode = {}", serviceCode);
			
			/* 設定 request headers */
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
			headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
			log.info("ReplyMessageActor-onReceive : headers = {}", headers);

			/* 將 headers 跟 body 塞進 HttpEntity 中 */
			HttpEntity<String> httpEntity = new HttpEntity<String>(message.toString(), headers);
			log.info("ReplyMessageActor-onReceive : httpEntity = {}", httpEntity);

			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_REPLY_URL.toString()), httpEntity);

			JSONObject jsonObjectResult = restfulUtil.execute();
			log.info("ReplyMessageActor-onReceive : jsonObjectResult = {}", jsonObjectResult.toString());
		}
	}
}