package com.bcs.core.utils;

import org.apache.commons.lang3.StringUtils;

public class LineIdUtil {

	public static Boolean isLineUID(String input){
		// With UID LINE v2
		if(StringUtils.isNotBlank(input) && 33 == input.length() && input.startsWith("U") && input.matches("[a-zA-Z0-9|\\.]*")){
			return true;
		}
		
		return false;
	}

	public static Boolean isLineMID(String input){
		// With MID LINE v1
		if(StringUtils.isNotBlank(input) && 33 == input.length() && input.startsWith("u") && input.matches("[a-zA-Z0-9|\\.]*")){
			return true;
		}
		
		return false;
	}
}
