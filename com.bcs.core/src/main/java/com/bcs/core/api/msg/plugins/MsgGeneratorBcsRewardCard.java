package com.bcs.core.api.msg.plugins;

import java.util.Map;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorLink;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;

public class MsgGeneratorBcsRewardCard extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorLink.canSetMulti();
	}

	private MsgGeneratorLink msgLink;
	
	private final ContentRewardCard contentRewardCard;

	private final String textParams;
	private final String linkUriParams;

	public MsgGeneratorBcsRewardCard(ObjectNode node) throws Exception{
		super();
		
		String rewardCardId = node.get("rewardCardId").textValue();
		
		this.contentRewardCard = ApplicationContextProvider.getApplicationContext().getBean(ContentRewardCardService.class).findOne(rewardCardId);

		this.textParams = contentRewardCard.getRewardCardMainTitle();
		this.linkUriParams = UriHelper.getRewardCardPattern(rewardCardId);
		
		createMsgLink();
	}

	public MsgGeneratorBcsRewardCard(MsgDetail detail) throws Exception{
		super();
		String rewardCardId = detail.getReferenceId();

		this.contentRewardCard = ApplicationContextProvider.getApplicationContext().getBean(ContentRewardCardService.class).findOne(rewardCardId);

		this.textParams = contentRewardCard.getRewardCardMainTitle();
		this.linkUriParams = UriHelper.getRewardCardPattern(rewardCardId);
		
		createMsgLink(detail);
	}

	private void createMsgLink() throws Exception{

		String bcsLogoUri = UriHelper.getResourceBcsLogoUri();
		
		String linkTextParams = "領取";
		
		msgLink = new MsgGeneratorLink(bcsLogoUri, textParams, linkTextParams, linkUriParams);
	}

	private void createMsgLink(MsgDetail detail) throws Exception{

		String bcsLogoUri = UriHelper.getResourceBcsLogoUri();

		String linkTextParams = "領取";
		
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
	
}
