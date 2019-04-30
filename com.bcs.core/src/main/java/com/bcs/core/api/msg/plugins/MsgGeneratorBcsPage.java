package com.bcs.core.api.msg.plugins;

import java.util.Map;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorLink;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.resource.UriHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;

public class MsgGeneratorBcsPage extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorLink.canSetMulti();
	}

	private MsgGeneratorLink msgLink;
	
	private final String linkUriParams;

	/**
	 * @param textParams
	 * @param subTextParams
	 * @param linkTextParams
	 * @param linkUriParams
	 * @throws Exception
	 */
	public MsgGeneratorBcsPage() throws Exception{
		super();
		
		linkUriParams = UriHelper.getGoIndexUri();
		
		createMsgLink();
	}

	public MsgGeneratorBcsPage(ObjectNode node) throws Exception{
		super();

		linkUriParams = UriHelper.getGoIndexUri();
		
		createMsgLink();
	}

	public MsgGeneratorBcsPage(MsgDetail detail) throws Exception{
		super();

		linkUriParams = UriHelper.getGoIndexUri();
		
		createMsgLink();
	}

	private void createMsgLink() throws Exception{
		
		String bcsLogoUri = UriHelper.getResourceBcsLogoUri();
		
		String templateId = MsgGeneratorLink.templateId_Sub;
		String textParams = "卡友頁面";
		String subTextParams = "BCS 卡友頁面";
		String linkTextParams = "前往";
		
		msgLink = new MsgGeneratorLink(bcsLogoUri, templateId, textParams, subTextParams, linkTextParams, linkUriParams);
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
}
