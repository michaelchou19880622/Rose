package com.bcs.core.api.msg;

import java.util.Map;

import com.bcs.core.json.AbstractBcsEntity;
import com.linecorp.bot.model.message.Message;

public abstract class MsgGeneratorAbstract extends AbstractBcsEntity implements MsgGenerator  {
	private static final long serialVersionUID = 1L;
	
	public static final String REPLACE_MSG_MID = "MSG_REPLACE_MID";
	
	protected Message message;
	
	protected MsgGeneratorAbstract(){
		
	}

	public Message getMessageBot(String toMid) throws Exception{
		return message;
	}

	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{
		return message;
	}
}
