package com.bcs.core.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LiveChatWordingUtil {
	public static String getString(String key) {
		Locale locale = new Locale("zh", "TW");
		ResourceBundle resourceBundle = ResourceBundle.getBundle("config.liveChatWording", locale);
		
		return resourceBundle.getString(key);
	}
}