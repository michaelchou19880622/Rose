package com.bcs.core.api.msg;

import java.util.Map;

import com.linecorp.bot.model.message.Message;

public interface MsgGenerator {
	public static final String MSG_TYPE_TEXT = "TEXT";
	public static final String MSG_TYPE_IMAGE = "IMAGE";
	public static final String MSG_TYPE_AUDIO = "AUDIO";
	public static final String MSG_TYPE_VIDEO = "VIDEO";
	public static final String MSG_TYPE_LOCATION = "LOCATION";
	public static final String MSG_TYPE_STICKER = "STICKER";
	public static final String MSG_TYPE_RICH_MSG = "RICH_MSG";
	public static final String MSG_TYPE_LINK = "LINK";
	public static final String MSG_TYPE_TEMPLATE = "TEMPLATE";
	
	public abstract Message getMessageBot(String toMid) throws Exception;

	public abstract Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception;
}
