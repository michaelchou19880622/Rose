package com.bcs.core.taishin.api.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bcs.core.api.service.model.PushApiModel;

public class PushApiRequestValidator {
	public static void validate(String requestBodyString, PushApiModel pushApiModel) throws IllegalArgumentException, ParseException {
		String department = null;
		JSONArray uids = null;
		String sendTimeType = null;
		Date sendTimeSet = null;
		JSONArray messageList = null;
		JSONObject requestBody = new JSONObject(requestBodyString);
		
		if(!requestBody.has("department"))
			throw new IllegalArgumentException("Parameter 'department' is required.");
		else if(requestBody.getString("department").equals(""))
			throw new IllegalArgumentException("Parameter 'department' cannot be empty.");
		else
			department = requestBody.getString("department");
		
		if(!requestBody.has("uid"))
			throw new IllegalArgumentException("Parameter 'uid' is required.");
		else {
			String uid = requestBody.get("uid").toString();
			
			if(uid.trim().charAt(0) != '[')
				throw new IllegalArgumentException("Parameter 'uid' must be an array.");
			
			uids = new JSONArray(uid);
			
			if(uids.length() == 0)
				throw new IllegalArgumentException("Parameter 'uid' cannot be empty.");
		}
		
		if(!requestBody.has("sendTimeType"))
			throw new IllegalArgumentException("Parameter 'sendTimeType' is required.");
		else if(requestBody.getString("sendTimeType").equals(""))
			throw new IllegalArgumentException("Parameter 'sendTimeType' cannot be empty.");
		else if(!requestBody.getString("sendTimeType").equals(PushApiModel.SEND_TYPE_DELAY) && !requestBody.getString("sendTimeType").equals(PushApiModel.SEND_TYPE_IMMEDIATE))
			throw new IllegalArgumentException("Invalid value of parameter 'sendTimeType'.");
		else
			sendTimeType = requestBody.getString("sendTimeType");
		
		if(sendTimeType.equals(PushApiModel.SEND_TYPE_DELAY)) {
			if(!requestBody.has("sendTimeSet"))
				throw new IllegalArgumentException("Parameter 'sendTimeSet' is required.");
			else if(requestBody.getString("sendTimeSet").equals(""))
				throw new IllegalArgumentException("Parameter 'sendTimeSet' cannot be empty.");
			else if (!requestBody.getString("sendTimeSet").matches("(\\d{4})-([0-1]\\d)-([0-3]\\d) ([0-2][0-9]):([0-6]\\d):([0-6]\\d)"))
				throw new IllegalArgumentException("Invalid format of parameter 'sendTimeSet', must be 'yyyy-MM-dd hh:mm:ss'.");
			else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				sendTimeSet = sdf.parse(requestBody.getString("sendTimeSet"));
			}
				
		}
		
		if(!requestBody.has("messages"))
			throw new IllegalArgumentException("Parameter 'messages' is required.");
		else {
			String messages = requestBody.get("messages").toString();
			
			if(messages.trim().charAt(0) != '[')
				throw new IllegalArgumentException("Parameter 'messages' must be an array.");
			
			messageList = new JSONArray(messages);
			
			if(messageList.length() == 0)
				throw new IllegalArgumentException("Parameter 'messages' cannot be empty.");
		}
		
		pushApiModel.setDepartment(department);
		pushApiModel.setUid(uids);
		pushApiModel.setSendTimeType(sendTimeType);
		pushApiModel.setSendTimeSet(sendTimeSet);
		pushApiModel.setMessages(messageList);
		pushApiModel.setTriggerTime(new Date());
		
		return;
	}
}
