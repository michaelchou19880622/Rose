package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsLink;
import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.ContentLinkTracingService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ContentLinkTracingUIService {
	@Autowired
	private ContentLinkTracingService contentLinkTracingService;
	@Autowired
	private ContentFlagService contentFlagService;
	@Autowired
	private ContentLinkService contentLinkService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentLinkTracingUIService.class);

	public Long generateTracingLink(SendMsgDetailModel linkData, SendMsgDetailModel linkBindedData, SendMsgDetailModel linkUnMobile, String adminUserAccount) throws Exception{
		return this.generateTracingLink(null, linkData, linkBindedData, linkUnMobile, adminUserAccount);
	}
	
	public Long generateTracingLink(Long tracingId, SendMsgDetailModel linkData, SendMsgDetailModel linkBindedData, SendMsgDetailModel linkUnMobileData, String adminUserAccount) throws Exception{
		logger.debug("generateTracingLink:" + linkData);

		String detailType = linkData.getDetailType();
		if(MsgGenerator.MSG_TYPE_LINK.equals(detailType)){
			
			ContentLinkTracing tracingLink = null;
			
			Date date = new Date();
			
			if(tracingId != null){
				tracingLink = contentLinkTracingService.findOne(tracingId);
				if(tracingLink == null){
					throw new BcsNoticeException("資料格式錯誤");
				}
			}
			else{
				tracingLink = new ContentLinkTracing();
			}
	
			String linkId = saveLink(linkData, adminUserAccount, date);
			
			String linkIdBinded = saveLink(linkBindedData, adminUserAccount, date);
			
			String linkIdUnMobile = saveLink(linkUnMobileData, adminUserAccount, date);
			
			tracingLink.setLinkId(linkId);
			tracingLink.setLinkIdBinded(linkIdBinded);
			tracingLink.setLinkIdUnMobile(linkIdUnMobile);
			tracingLink.setModifyTime(date);
			tracingLink.setModifyUser(adminUserAccount);
			
			contentLinkTracingService.save(tracingLink);
			
			return tracingLink.getTracingId();
		}
		else{
			throw new BcsNoticeException("資料格式錯誤");
		}
	}
	
	private String saveLink(SendMsgDetailModel linkData, String adminUserAccount, Date date) throws Exception{

		String linkId = UUID.randomUUID().toString().toLowerCase();
		
		ObjectNode node = ObjectUtil.jsonStrToObjectNode(linkData.getDetailContent());

		MsgGeneratorBcsLink link = new MsgGeneratorBcsLink(node);
		
		ContentLink contentLink = new ContentLink();
		contentLink.setLinkId(linkId);
		contentLink.setLinkUrl(link.getLinkUriParams());
		contentLink.setLinkTitle(link.getTextParams());
		
		List<String> linkTagList = new ArrayList<>();
		
		for (JsonNode linkTag : node.get("linkTagList")) {
			linkTagList.add(linkTag.asText());
		}
		
		contentLink.setLinkTag(contentFlagService.concat(linkTagList, 50));
		contentLink.setModifyTime(date);
		contentLink.setModifyUser(adminUserAccount);
		contentLinkService.save(contentLink);
		
		// Save ContentFlag
		contentFlagService.save(
				linkId, 
				ContentFlag.CONTENT_TYPE_LINK, 
				linkTagList);
		
		return linkId;
	}
}
