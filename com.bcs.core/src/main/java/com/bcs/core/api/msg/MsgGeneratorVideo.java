package com.bcs.core.api.msg;


import org.apache.commons.lang3.StringUtils;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.resource.UriHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.VideoMessage;

public class MsgGeneratorVideo extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return true;
	}

	private String originalContentUrl;
	private String previewImageUrl;
	
	public MsgGeneratorVideo(String originalContentUrl, String previewImageUrl) throws Exception{
		super();
		generatorContent(originalContentUrl, previewImageUrl, 1);
		
		message = new VideoMessage(originalContentUrl, previewImageUrl);
	}
	
	public MsgGeneratorVideo(String originalContentUrl, String previewImageUrl, int toType) throws Exception{
		super();
		generatorContent(originalContentUrl, previewImageUrl, toType);
		
		message = new VideoMessage(originalContentUrl, previewImageUrl);
	}
	
	public MsgGeneratorVideo(ObjectNode node) throws Exception{
		super();
		String originalContentUrl = node.get("originalContentUrl").textValue();
		String previewImageUrl = node.get("previewImageUrl").textValue();
		
		if (node.get("toType") == null) {
			generatorContent(originalContentUrl, previewImageUrl, 1);
			
			message = new VideoMessage(originalContentUrl, previewImageUrl);
		} else {
			generatorContent(originalContentUrl, previewImageUrl, node.get("toType").intValue());
			
			message = new VideoMessage(originalContentUrl, previewImageUrl);
		}
	}

	public MsgGeneratorVideo(MsgDetail detail) throws Exception{
		super();
		generatorContent(detail, 1);
		
		message = new VideoMessage(originalContentUrl, previewImageUrl);
	}
	
	public MsgGeneratorVideo(MsgDetail detail, int toType) throws Exception{
		super();
		generatorContent(detail, toType);
		
		message = new VideoMessage(originalContentUrl, previewImageUrl);
	}
	
	private void generatorContent(MsgDetail detail, int toType) throws Exception{

		String originalContentUrl = UriHelper.getResourceUri(detail.getMsgType(), detail.getReferenceId());
		String previewImageUrl = UriHelper.getResourcePreviewUri(detail.getMsgType(), "IMAGE", detail.getReferenceId());
		
		generatorContent(originalContentUrl, previewImageUrl, toType);
	}
	
	private void generatorContent(String originalContentUrl, String previewImageUrl, int toType) throws Exception{
		
		if(StringUtils.isBlank(originalContentUrl)){
			throw new Exception("originalContentUrl Can not be Null");
		}

		if(StringUtils.isBlank(previewImageUrl)){
			throw new Exception("previewImageUrl Can not be Null");
		}

		this.originalContentUrl = originalContentUrl;
		this.previewImageUrl = previewImageUrl;
	}

	public String getOriginalContentUrl() {
		return originalContentUrl;
	}

	public String getPreviewImageUrl() {
		return previewImageUrl;
	}
}
