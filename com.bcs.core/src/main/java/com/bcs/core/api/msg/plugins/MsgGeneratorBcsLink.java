package com.bcs.core.api.msg.plugins;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorLink;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;

public class MsgGeneratorBcsLink extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorLink.canSetMulti();
	}

	private MsgGeneratorLink msgLink;

	private final String textParams;
	private final String linkUriParams;
	private final String linkImageParams;

	public MsgGeneratorBcsLink(ObjectNode node) throws Exception{
		super();

		this.textParams = node.get("textParams").asText();
		this.linkUriParams =  node.get("linkUriParams").asText();
		if(node.get("linkPreviewImage") != null){
			this.linkImageParams = node.get("linkPreviewImage").asText();
		}
		else{
			this.linkImageParams = null;
		}
		
		createMsgLink();
	}

	public MsgGeneratorBcsLink(MsgDetail detail) throws Exception{
		super();

		this.textParams = detail.getText();
		
		String serialId = getSerialId(detail);
		if(serialId != null){
			this.linkUriParams = UriHelper.getSerialIdLinkPattern(detail.getReferenceId(), serialId);
		}
		else{
			this.linkUriParams = UriHelper.getLinkPattern(detail.getReferenceId());
		}
		this.linkImageParams = getImageId(detail.getReferenceId());
		
		createMsgLink(detail);
	}
	
	private String getImageId(String referenceId){
		ContentLinkService contentLinkService = ApplicationContextProvider.getApplicationContext().getBean(ContentLinkService.class);

		ContentLink contentLink = contentLinkService.findOne(referenceId);
		
		return contentLink.getLinkPreviewImage();
	}
	
	private String getSerialId(MsgDetail detail){
		
		Long msgId = detail.getMsgId();
		
		String msgParentType = detail.getMsgParentType();
		
		if(MsgMain.THIS_PARENT_TYPE.equals(msgParentType)){
			MsgMainService msgMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgMainService.class);
			MsgMain msgMain = msgMainService.findOne(msgId);
			if(msgMain != null && StringUtils.isNotBlank(msgMain.getSerialId())){
				return msgMain.getSerialId();
			}
		}
		else if(MsgSendMain.THIS_PARENT_TYPE.equals(msgParentType)){
			MsgSendMainService msgSendMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class);
			MsgSendMain msgSendMain = msgSendMainService.findOne(msgId);
			if(msgSendMain != null && StringUtils.isNotBlank(msgSendMain.getSerialId())){
				return msgSendMain.getSerialId();
			}
		}
		else if(MsgInteractiveMain.THIS_PARENT_TYPE.equals(msgParentType)){
			MsgInteractiveMainService msgInteractiveMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgInteractiveMainService.class);
			MsgInteractiveMain msgInteractiveMain = msgInteractiveMainService.findOne(msgId);
			if(msgInteractiveMain != null && StringUtils.isNotBlank(msgInteractiveMain.getSerialId())){
				return msgInteractiveMain.getSerialId();
			}
		}
		
		return null;
	}

	private void createMsgLink() throws Exception{
		
		String bcsLogoUri = UriHelper.getResourceBcsLogoUri();
		if(StringUtils.isNotBlank(linkImageParams)){
			bcsLogoUri = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, linkImageParams);
		}
		
		String linkTextParams = "前往";
		
		msgLink = new MsgGeneratorLink(bcsLogoUri, textParams, linkTextParams, linkUriParams);
	}

	private void createMsgLink(MsgDetail detail) throws Exception{

		String bcsLogoUri = UriHelper.getResourceBcsLogoUri();
		if(StringUtils.isNotBlank(linkImageParams)){
			bcsLogoUri = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, linkImageParams);
		}
		
		String linkTextParams = "前往";
		
		msgLink = new MsgGeneratorLink(bcsLogoUri, textParams, linkTextParams, linkUriParams);
	}

	@Override
	public Message getMessageBot(String toMid) throws Exception{
		return getMessageBot(toMid, null);
	}

	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{
		// Change Url
		String url = UriHelper.parseBcsPage(linkUriParams, toMid);
		msgLink.updateLinkUri(url);
		
		return msgLink.getMessageBot(toMid, replaceParam);
	}

	public String getTextParams() {
		return textParams;
	}

	public String getLinkUriParams() {
		return linkUriParams;
	}

	public String getLinkImageParams() {
		return linkImageParams;
	}
	
}
