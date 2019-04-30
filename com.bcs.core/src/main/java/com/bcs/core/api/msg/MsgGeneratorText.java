package com.bcs.core.api.msg;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.db.entity.MsgDetail;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

public class MsgGeneratorText extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return true;
	}

	private String text;
	
	public MsgGeneratorText(String text) throws Exception{
		super();
		generatorContent(text, 1);
		
		message = new TextMessage(text);
	}
	
	public MsgGeneratorText(String text, int toType) throws Exception{
		super();
		generatorContent(text, toType);
		
		message = new TextMessage(text);
	}
	
	public MsgGeneratorText(ObjectNode node) throws Exception{
		super();
		if(node.get("toType") == null){
			generatorContent(node.get("Text").textValue(), 1);
			
			message = new TextMessage(text);
		}
		else{
			generatorContent(node.get("Text").textValue(), node.get("toType").intValue());
			
			message = new TextMessage(text);
		}
	}

	public MsgGeneratorText(MsgDetail detail) throws Exception{
		super();
		generatorContent(detail.getText(), 1);
		
		message = new TextMessage(text);
	}
	
	public MsgGeneratorText(MsgDetail detail, int toType) throws Exception{
		super();
		generatorContent(detail.getText(), toType);
		
		message = new TextMessage(text);
	}
	
	private void generatorContent(String text, int toType) throws Exception{
		
		if(StringUtils.isBlank(text)){
			throw new Exception("text Can not be Null");
		}
		
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{
		
		if(replaceParam != null && replaceParam.size() > 0){
			String replace = text;
			
			for(String replcaTarget : replaceParam.keySet()){
				replace = replace.replaceAll("\\{" + replcaTarget + "\\}", replaceParam.get(replcaTarget));
			}
			
			return new TextMessage(replace);
		}
		
		return message;
	}
}
