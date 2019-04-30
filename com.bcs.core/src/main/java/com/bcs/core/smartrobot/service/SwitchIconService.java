package com.bcs.core.smartrobot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.linecorp.bot.model.message.Sender;

@Service
public class SwitchIconService {
	public void appendSender(String channelId,JSONObject responseObject) {
		JSONArray messages = responseObject.getJSONArray("messages");
		for(int i=0;i<messages.length();i++) {
			messages.getJSONObject(i).put("sender", generateSenderJSONObject(channelId));
		}
	}
	
	public JSONObject generateSenderJSONObject(String channelId) {
		String name = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSwitchIconName.toString(), true);
		String iconUrl = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSwitchIconUrl.toString(), true);
		JSONObject senderObject = new JSONObject();
		senderObject.put("iconUrl",iconUrl);
		senderObject.put("name", name);
		return senderObject;
	}
	
	public Sender generateSenderModel(String channelId) {
		String name = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSwitchIconName.toString(), true);
		String iconUrl = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelSwitchIconUrl.toString(), true);
		return new Sender(name, iconUrl);
	}
}
