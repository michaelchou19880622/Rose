package com.bcs.core.bot.send.akka.handler;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.RestfulUtil;

import akka.actor.UntypedActor;

public class ReplyMessageActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof JSONObject) {
			String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(), CONFIG_STR.ChannelServiceCode.toString(), true);
			/* 設定 request headers */
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
			headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
			
			/* 將 headers 跟 body 塞進 HttpEntity 中 */
			HttpEntity<String> httpEntity = new HttpEntity<String>(message.toString(), headers);
			
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_REPLY_URL.toString()), httpEntity);
			
			restfulUtil.execute();
		}
	}
}