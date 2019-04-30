package com.bcs.core.api.msg;



import org.apache.commons.lang3.StringUtils;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.resource.UriHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.AudioMessage;


public class MsgGeneratorAudio extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return true;
	}

	private String originalContentUrl;
	private String AUDLEN;
	
	public MsgGeneratorAudio(String originalContentUrl, String AUDLEN) throws Exception{
		super();
		generatorContent(originalContentUrl, AUDLEN, 1);
		
		message = new AudioMessage(originalContentUrl, Integer.parseInt(AUDLEN));
	}
	
	public MsgGeneratorAudio(String originalContentUrl, String AUDLEN, int toType) throws Exception{
		super();
		generatorContent(originalContentUrl, AUDLEN, toType);
		
		message = new AudioMessage(originalContentUrl, Integer.parseInt(AUDLEN));
	}
	
	public MsgGeneratorAudio(ObjectNode node) throws Exception{
		super();
		if(node.get("toType") == null){
			generatorContent(node.get("originalContentUrl").textValue(), node.get("AUDLEN").textValue(), 1);
			
			message = new AudioMessage(originalContentUrl, Integer.parseInt(AUDLEN));
		}
		else{
			generatorContent(node.get("originalContentUrl").textValue(), node.get("AUDLEN").textValue(), node.get("toType").intValue());
			
			message = new AudioMessage(originalContentUrl, Integer.parseInt(AUDLEN));
		}
	}

	public MsgGeneratorAudio(MsgDetail detail) throws Exception{
		super();
		generatorContent(detail, 1);
		
		message = new AudioMessage(originalContentUrl, Integer.parseInt(AUDLEN));
	}
	
	public MsgGeneratorAudio(MsgDetail detail, int toType) throws Exception{
		super();
		generatorContent(detail, toType);
		
		message = new AudioMessage(originalContentUrl, Integer.parseInt(AUDLEN));
	}
	
	private void generatorContent(MsgDetail detail, int toType) throws Exception{

		String originalContentUrl = UriHelper.getStaticResourceUri(detail.getMsgType(), detail.getReferenceId());
		if(StringUtils.isBlank(originalContentUrl)){
			originalContentUrl = UriHelper.getResourceUri(detail.getMsgType(), detail.getReferenceId());
		}
		String AUDLEN = "3000";
		
		generatorContent(originalContentUrl, AUDLEN, toType);
	}
	
	private void generatorContent(String originalContentUrl, String AUDLEN, int toType) throws Exception{
		
		if(StringUtils.isBlank(originalContentUrl)){
			throw new Exception("originalContentUrl Can not be Null");
		}

		if(StringUtils.isBlank(AUDLEN)){
			throw new Exception("AUDLEN Can not be Null");
		}

		this.originalContentUrl = originalContentUrl;
		this.AUDLEN = AUDLEN;
	}

	public String getOriginalContentUrl() {
		return originalContentUrl;
	}

	public String getAUDLEN() {
		return AUDLEN;
	}
	
}
