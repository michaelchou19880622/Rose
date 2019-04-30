package com.bcs.chatbot.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bcs.core.api.service.ChatBotApiService;

import junit.framework.TestCase;

public class SwitchIconTest extends TestCase{
	private static Logger logger = Logger.getLogger(SwitchIconTest.class);
	
	@Autowired
	private ChatBotApiService chatBotApiService;
	
	@Test
	public void test() {
		try {
			String responseJsonString="{\"messages\":[{\"text\":\"您好\",\"type\":\"text\"}],\"replyToken\":\"e3c6b73145e6470b95edb60d16e26e99\"}";
//			String responseJsonString = "{\"output\":\"您好\",\"line\":{\"messages\":[{\"text\":\"您好\",\"type\":\"text\"}],\"to\":\"U3ab928d67a9984e2728bb8560d3bb61f\"}}";
			JSONObject responseObject = new JSONObject(responseJsonString);
			logger.info(responseObject);
			appendSender(responseObject);
			logger.info(responseObject);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
		}
	}
	
	public void appendSender(JSONObject responseObject) {
		JSONArray messages = responseObject.getJSONArray("messages");
		logger.info("message.length()" + messages.length());
		for(int i=0;i<messages.length();i++) {
			messages.getJSONObject(i).put("sender", generateSenderModel());
		}
	}
	
	public JSONObject generateSenderModel() {
		JSONObject senderObject = new JSONObject();
		senderObject.put("iconUrl", "https://pngimg.com/uploads/phone/phone_PNG48972.png");
		senderObject.put("name", "智能客服");
		return senderObject;
	}

}
