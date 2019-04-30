package com.bcs.core.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectUtil {

	public static String listStringToString(List<String> list){
		if(list != null && list.size() > 0){
			if(list.size() == 1){
				return list.get(0);
			}
			
			String result = list.get(0);
			for(int i = 1; i < list.size(); i++){
				result += "," + list.get(i);
			}
			return result;
		}
		return null;
	}

	public static String listObjectToString(List<Object> list){
		List<String> s = new ArrayList<String>();
		for(Object o : list){
			s.add(ObjectUtil.objectToJsonStr(o));
		}
		return ObjectUtil.listStringToString(s);
	}

	public static String objectToJsonStr(Object content) {
	    try {
	    	return (new ObjectMapper()).writeValueAsString(content);
		} catch (JsonProcessingException e) {
			return content.getClass().getName() + '@' + Integer.toHexString(content.hashCode());
		}
	}

	public static ObjectNode jsonStrToObjectNode(String jsonStr) throws JsonProcessingException, IOException {
		return (ObjectNode)(new ObjectMapper()).readTree(jsonStr);
	}

	public static ArrayNode jsonStrToArrayNode(String jsonStr) throws JsonProcessingException, IOException {
		return (ArrayNode)(new ObjectMapper()).readTree(jsonStr);
	}

	public static JsonNode jsonStrToJsonNode(String jsonStr) throws JsonProcessingException, IOException {
		return new ObjectMapper().readTree(jsonStr);
	}

	public static <T> T jsonStrToObject(String jsonStr, Class<T> targetClass) throws JsonProcessingException, IOException {
		return new ObjectMapper().readValue(jsonStr, targetClass);
	}
}
