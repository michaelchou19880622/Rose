package com.bcs.core.utils;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;

public class MessageParseToBcObject {
	
	public static Message parseToMessage(JsonNode model) throws Exception{

		String type = validateNotBlank(model, "type");
		
		if("text".equals(type)){
			String text = validateNotBlank(model, "text");
			Message result = new TextMessage(text);
			return result;
		}
		else if("image".equals(type)){

			String originalContentUrl = validateNotBlank(model, "originalContentUrl");
			String previewImageUrl = validateNotBlank(model, "previewImageUrl");
			Message result = new ImageMessage(originalContentUrl, previewImageUrl);
			return result;
		}
		else if("video".equals(type)){

			String originalContentUrl = validateNotBlank(model, "originalContentUrl");
			String previewImageUrl = validateNotBlank(model, "previewImageUrl");
			Message result = new VideoMessage(originalContentUrl, previewImageUrl);
			return result;
		}
		else if("audio".equals(type)){

			String originalContentUrl = validateNotBlank(model, "originalContentUrl");
			Integer duration = validateNotBlankInt(model, "duration");
			Message result = new AudioMessage(originalContentUrl, duration);
			return result;
		}
		else if("location".equals(type)){

			String title = validateNotBlank(model, "title");
			String address = validateNotBlank(model, "address");
			double latitude = validateNotBlankDouble(model, "latitude");
			double longitude = validateNotBlankDouble(model, "longitude");
			Message result = new LocationMessage(title, address, latitude, longitude);
			return result;
		}
		else if("sticker".equals(type)){

			String packageId = validateNotBlank(model, "packageId");
			String stickerId = validateNotBlank(model, "stickerId");
			Message result = new StickerMessage(packageId, stickerId);
			return result;
		}
		
		throw new Exception("MessageError:ParseError");
	}
	
	private static String validateNotBlank(JsonNode model, String target) throws Exception{

		if(model.get(target) != null && StringUtils.isNotBlank(model.get(target).asText())){
			// Validate
			return model.get(target).asText();
		}
		else{
			throw new Exception("MessageError:" + target + "Null");
		}
	}
	
	private static Integer validateNotBlankInt(JsonNode model, String target) throws Exception{

		if(model.get(target) != null && StringUtils.isNotBlank(model.get(target).asText())){
			// Validate
			return model.get(target).asInt();
		}
		else{
			throw new Exception("MessageError:" + target + "Null");
		}
	}
	
	private static Double validateNotBlankDouble(JsonNode model, String target) throws Exception{

		if(model.get(target) != null && StringUtils.isNotBlank(model.get(target).asText())){
			// Validate
			return model.get(target).asDouble();
		}
		else{
			throw new Exception("MessageError:" + target + "Null");
		}
	}
}
