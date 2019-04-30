package com.bcs.core.api.msg.plugins;

import java.util.Map;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorLink;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;

public class MsgGeneratorBcsCoupon extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorLink.canSetMulti();
	}

	private MsgGeneratorLink msgLink;
	
	private final ContentCoupon contentCoupon;

	private final String textParams;
	private final String linkUriParams;

	public MsgGeneratorBcsCoupon(ObjectNode node) throws Exception{
		super();
		
		String couponId = node.get("couponId").textValue();
		
		this.contentCoupon = ApplicationContextProvider.getApplicationContext().getBean(ContentCouponService.class).findOne(couponId);

		this.textParams = contentCoupon.getCouponTitle();
		this.linkUriParams = UriHelper.getCouponPattern(couponId);
		
		createMsgLink();
	}

	public MsgGeneratorBcsCoupon(MsgDetail detail) throws Exception{
		super();
		String couponId = detail.getReferenceId();

		this.contentCoupon = ApplicationContextProvider.getApplicationContext().getBean(ContentCouponService.class).findOne(couponId);

		this.textParams = contentCoupon.getCouponTitle();
		this.linkUriParams = UriHelper.getCouponPattern(couponId);
		
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
