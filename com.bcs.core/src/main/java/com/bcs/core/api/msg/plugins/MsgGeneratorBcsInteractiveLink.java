package com.bcs.core.api.msg.plugins;

import java.net.URLEncoder;
import java.util.Map;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorLink;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.linecorp.bot.model.message.Message;

public class MsgGeneratorBcsInteractiveLink extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorLink.canSetMulti();
	}

	private MsgGeneratorLink msgLink;

	private final String textParams;
	private final String linkUriParams;

	public MsgGeneratorBcsInteractiveLink(MsgDetail detail) throws Exception{
		super();
		
		ContentLink link = ApplicationContextProvider.getApplicationContext().getBean(ContentLinkService.class).findOne(detail.getReferenceId());

		this.textParams = link.getLinkTitle();
		
		String keyword = URLEncoder.encode(detail.getText(), "UTF-8");
		
		this.linkUriParams = UriHelper.getReplaceLinkPattern(detail.getReferenceId(), keyword);
		
		createMsgLink(detail);
	}

	private void createMsgLink(MsgDetail detail) throws Exception{

		String bcsLogoUri = UriHelper.getResourceBcsLogoUri();
		
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
	
}
