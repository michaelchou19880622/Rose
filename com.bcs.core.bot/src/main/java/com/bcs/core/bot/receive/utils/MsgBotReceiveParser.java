package com.bcs.core.bot.receive.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.utils.ErrorRecord;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linecorp.bot.model.event.CallbackRequest;

public class MsgBotReceiveParser {
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgBotReceiveParser.class);

    private static ObjectMapper objectMapper;

    public static void buildObjectMapper() {
    	if(objectMapper == null){
    		objectMapper = new ObjectMapper();
	        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	        // Register JSR-310(java.time.temporal.*) module and read number as millsec.
	        objectMapper.registerModule(new JavaTimeModule())
	                    .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    	}
    }
    
    public static CallbackRequest parseMessage(String receiveMsg) throws Exception{

		buildObjectMapper();
		final CallbackRequest callbackRequest = objectMapper.readValue(receiveMsg, CallbackRequest.class);
        if (callbackRequest == null || callbackRequest.getEvents() == null) {
        	throw new Exception("Invalid content");
        }
        
        return callbackRequest;
    }
	
	public static JsonNode parseMessageGetByCount(String receiveMsg, Integer count){

		try {
			JsonNode events = new ObjectMapper().readTree(receiveMsg).get("events");
			if (events.isArray()) {
				int i = 0; 
				for (JsonNode msg : events) {
					if(i == count){
						ObjectNode obj = (new ObjectMapper()).createObjectNode();
						List<JsonNode> list = new ArrayList<JsonNode>();
						list.add(msg);
						obj.putPOJO("events", list);
						return obj;
					}
					
					i++;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		return null;
	}
	
	public static List<MsgBotReceive> parseMessage(String receiveMsg, String channelId){
		List<MsgBotReceive> msgReceives = new ArrayList<MsgBotReceive>();
		
		try {
			JsonNode events = new ObjectMapper().readTree(receiveMsg).get("events");
			if (events.isArray()) {
				for (JsonNode msg : events) {

					String others = "";
					 
					MsgBotReceive receive = new MsgBotReceive();
					
					receive.setChannel(channelId);

					if(msg.get("replyToken") != null && !msg.get("replyToken").isNull()){
						receive.setReplyToken(msg.get("replyToken").asText());
					}
					receive.setEventType(msg.get("type").asText());
					
					Long timestamp = msg.get("timestamp").asLong();
					receive.setTimestamp(timestamp);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date now = new Date(timestamp);
					receive.setReceiveTime(now);
					receive.setReceiveDay(sdf.format(now));

					if(msg.get("source") != null && !msg.get("source").isNull()){
						String type = msg.get("source").get("type").asText();
						receive.setSourceType(type);
						if("user".equals(type)){
							receive.setSourceId(msg.get("source").get("userId").asText());
						}
						else if("group".equals(type)){
							receive.setSourceId(msg.get("source").get("groupId").asText());
						}
						else if("room".equals(type)){
							receive.setSourceId(msg.get("source").get("roomId").asText());
						}
					}
					
					if(msg.get("message") != null && !msg.get("message").isNull()){
						receive.setMsgId(msg.get("message").get("id").asText());
						receive.setMsgType(msg.get("message").get("type").asText());
						
						if(msg.get("message").get("text") != null && !msg.get("message").get("text").isNull()){
							receive.setText(msg.get("message").get("text").asText());
						}

						try{
							if(msg.get("message").get("title") != null && !msg.get("message").get("title").isNull()){
								receive.setLocationTitle(msg.get("message").get("title").asText());
							}
							if(msg.get("message").get("address") != null && !msg.get("message").get("address").isNull()){
								receive.setLocationAddress(msg.get("message").get("address").asText());
							}
							if(msg.get("message").get("latitude") != null && !msg.get("message").get("latitude").isNull()){
								receive.setLocationLatitude(msg.get("message").get("latitude").asText());
							}
							if(msg.get("message").get("longitude") != null && !msg.get("message").get("longitude").isNull()){
								receive.setLocationLongitude(msg.get("message").get("longitude").asText());
							}
						}
						catch(Exception e){
							others += msg.get("message");
						} //Skip

						try{
							if(msg.get("message").get("packageId") != null && !msg.get("message").get("packageId").isNull()){
								receive.setStickerPackageId(msg.get("message").get("packageId").asText());
							}
							if(msg.get("message").get("stickerId") != null && !msg.get("message").get("stickerId").isNull()){
								receive.setStickerId(msg.get("message").get("stickerId").asText());
							}
						}
						catch(Exception e){
							others += msg.get("message");
						} //Skip
					}
					
					if(msg.get("postback") != null && !msg.get("postback").isNull()){
						try{
							receive.setPostbackData(msg.get("postback").get("data").asText());
						}
						catch(Exception e){
							others += msg.get("postback");
						} //Skip
					}
					
					if(msg.get("beacon") != null && !msg.get("beacon").isNull()){
						try{
							receive.setBeaconHwid(msg.get("beacon").get("hwid").asText());
							receive.setBeaconType(msg.get("beacon").get("type").asText());
						}
						catch(Exception e){
							others += msg.get("beacon");
						} //Skip
					}

					receive.setOthers(others);
					
					msgReceives.add(receive);
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		return msgReceives;
	}
}
