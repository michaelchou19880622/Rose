package com.bcs.core.linepoint.web.ui.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
//import com.linecorp.bot.model.richmenu.RichMenu;
//import com.linecorp.bot.model.richmenu.RichMenuArea;
//import com.linecorp.bot.model.richmenu.RichMenuBounds;
//import com.linecorp.bot.model.richmenu.RichMenuSize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// Core Original
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.api.service.model.PostLineResponse;

// Core Added
import com.bcs.core.linepoint.api.msg.model.LinePointAction;
import com.bcs.core.linepoint.api.service.LinePointLineApiService;
import com.bcs.core.linepoint.db.service.LinePointMsgContentLinkTracingService;


@Service
public class LinePointContentUIService {

	private static Logger logger = Logger.getLogger(LinePointContentUIService.class);
//	@Autowired
//	private RichMenuContentService contentRichMenuService;
//	@Autowired
//	private RichMenuContentDetailRepository contentRichMenuDetailRepository;
//	@Autowired
//	private RichMenuContentLinkService contentLinkService;
	@Autowired
	private LinePointLineApiService lineRichMenuApiService;
	@Autowired
	private ContentResourceService contentResourceService;
	
//	/**
//	 * 呼叫建立圖文選單API
//	 * 
//	 * @param contentRichMenu
//	 * @param contentRichMenuDetails
//	 * @param contentLinks
//	 * @return richMenuId
//	 * @throws BcsNoticeException 
//	 */
//	public String callCreateRichMenuAPI(String channelId, String richId) throws BcsNoticeException  {
//		logger.info(" ContentRichMenuUIService callCreateRichMenuAPI By richId");
//		
//		try{
//			RichMenuContent richMenuContent = contentRichMenuService.getSelectedContentRichMenu(richId);
//			List<RichMenuContentDetail> richMenuContentDetails = contentRichMenuDetailRepository.findByRichId(richId);
//			List<RichMenuContentLink> richMenuContentLink = contentLinkService.findContentLinkByRichId(richId);
//			
//			return callCreateRichMenuAPI(channelId, richMenuContent, richMenuContentDetails, richMenuContentLink, 0);
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			throw new BcsNoticeException(e.getMessage());
//		}
//		
//	}
//
//	/**
//	 * 呼叫建立圖文選單API
//	 * 
//	 * @param contentRichMenu
//	 * @param contentRichMenuDetails
//	 * @param contentLinks
//	 * @return richMenuId
//	 * @throws BcsNoticeException 
//	 */
//	public String callCreateRichMenuAPI(String channelId, RichMenuContent contentRichMenu, List<RichMenuContentDetail> contentRichMenuDetails, List<RichMenuContentLink> contentLinks, int retryCount) throws BcsNoticeException  {
//		logger.info(" ContentRichMenuUIService callCreateRichMenuAPI");
//		try{
//			Boolean selected = false;
//			if(RichMenuContent.STATUS_OPEN.equals(contentRichMenu.getRichMenuShowStatus())){
//				selected = true;
//			}
//			
//			List<RichMenuArea> areas = createRichMenuObject(contentRichMenuDetails, contentLinks);
//			
//			RichMenuSize menuSize = getRichMenuSizeObject(contentRichMenu.getMenuSize());
//			
//			RichMenu richMenu = RichMenu
//					.builder()
//					.name(contentRichMenu.getRichMenuName())// Rich Menu名稱，方便管理，不顯示在LINE上
//					.areas(areas)// 指定圖片特定範圍的動作
//					.chatBarText(contentRichMenu.getRichMenuTitle())// Rich Menu Bar 在Line上顯示的標題
//					.selected(selected)// 選單是否默認開啟
//					.size(menuSize)// Rich Menu 圖片尺寸
//					.build();
//			
//			PostLineResponse result = lineRichMenuApiService.callCreateRichMenuAPI(channelId, richMenu, 0);
//			if(result.getStatus() != 200){
//				if(result.getStatus() == 401){
//					if(retryCount < 5){
//						ObjectNode callRefreshingResult = LineAccessApiService.callVerifyAPIAndIssueToken(channelId, true);
//						logger.info("callRefreshingResult:" + callRefreshingResult);
//						return this.callCreateRichMenuAPI(channelId, contentRichMenu, contentRichMenuDetails, contentLinks, retryCount + 1);
//					}else{
//						throw new Exception("Create Rich Menu Error Times > 5");
//					}
//				}else{
//					throw new Exception(result.getResponseStr());
//				}
//			}
//			logger.debug("callCreateRichMenuAPI result:" + result);
//			JsonNode resultNode = new ObjectMapper().readTree(result.getResponseStr());
//			return resultNode.get("richMenuId").asText();
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			throw new BcsNoticeException(e.getMessage());
//		}
//		
//	}
//	
//	// 設定指定UID的圖文選單API
//	public void callLinkRichMenuToUserAPI(String richMenuId, String uid) throws BcsNoticeException  {
//		logger.info(" ContentRichMenuUIService callLinkRichMenuToUserAPI");
//		try{			
//			PostLineResponse result = lineRichMenuApiService.callLinkRichMenuToUserAPI(richMenuId, uid, 0);
//			if(result.getStatus() != 200){
//				throw new Exception(result.getResponseStr());
//			}
//			logger.debug("callLinkRichMenuToUserAPI result:" + result);			
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			throw new BcsNoticeException(e.getMessage());
//		}
//	}
//	
//	// 設定預設圖文選單API
//	public void callLinkRichMenuToAllUserAPI(String richMenuId) throws BcsNoticeException  {
//		logger.info(" ContentRichMenuUIService callLinkRichMenuToAllUserAPI");
//		try{			
//			PostLineResponse result = lineRichMenuApiService.callLinkRichMenuToAllUserAPI(richMenuId, 0);
//			if(result.getStatus() != 200){
//				throw new Exception(result.getResponseStr());
//			}
//			logger.debug("callLinkRichMenuToAllUserAPI result:" + result);			
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			throw new BcsNoticeException(e.getMessage());
//		}
//	}
//	
//	private List<RichMenuArea> createRichMenuObject(List<RichMenuContentDetail> contentRichMenuDetails, List<RichMenuContentLink> contentLinks) throws Exception{
//		List<RichMenuArea> areas = new ArrayList<RichMenuArea>();
//		
//		Map<String, RichMenuContentLink> linksMap = new HashMap<String, RichMenuContentLink>();
//		for(RichMenuContentLink contentLink : contentLinks){
//			linksMap.put(contentLink.getLinkId(), contentLink);
//		}
//
//		for(RichMenuContentDetail contentRichMenuDetail : contentRichMenuDetails){
//			areas.add(createRichArea(contentRichMenuDetail, linksMap));
//		}
//		
//		return areas;
//	}
//	
//	private RichMenuArea createRichArea(RichMenuContentDetail contentRichMenuDetail, Map<String, RichMenuContentLink> linksMap) throws Exception{
//		int startX = contentRichMenuDetail.getStartPointX();
//		int startY = contentRichMenuDetail.getStartPointY();
//		int width = contentRichMenuDetail.getEndPointX() - contentRichMenuDetail.getStartPointX();
//		int height = contentRichMenuDetail.getEndPointY() - contentRichMenuDetail.getStartPointY();
//		
//		String actionType = contentRichMenuDetail.getActionType();
//		
//		if(LinePointAction.ACTION_TYPE_SEND_MESSAGE.equals(actionType)){
//			return new RichMenuArea(new RichMenuBounds(startX, startY, width, height),
//                    new MessageAction(actionType, contentRichMenuDetail.getLinkId()));
//		}else if(LinePointAction.ACTION_TYPE_WEB.equals(actionType)){
//			String contentLinkId = contentRichMenuDetail.getLinkId();
//			RichMenuContentLink contentLink = linksMap.get(contentLinkId);
//			
//			logger.info("CL1:"+contentLink.toString());
//			// IKEA's original
//			//String url = ApplicationContextProvider.getApplicationContext().getBean(RichMenuMsgContentLinkTracingService.class).generateMsgTracingLink(contentLink);
//			String url = contentLink.getLinkUrl();
//			logger.info("url1:"+url);
//			
//			return new RichMenuArea(new RichMenuBounds(startX, startY, width, height),
//                    new URIAction(actionType, url));
//		}else if(LinePointAction.ACTION_TYPE_POSTBACK.equals(actionType)){
//			return new RichMenuArea(new RichMenuBounds(startX, startY, width, height),
//                    new PostbackAction(actionType, contentRichMenuDetail.getLinkId()));
//		}
//
//        return null;
//	}
//	
//	private RichMenuSize getRichMenuSizeObject(String menuSize){
//		if(RichMenuContent.SIZE_FULL.equals(menuSize)){
//			return RichMenuSize.FULL;
//		}else if(RichMenuContent.SIZE_HALF.equals(menuSize)){
//			return RichMenuSize.HALF;
//		}
//		return null;
//	}
//	
//	/**
//	 * 呼叫上傳圖片API
//	 * 
//	 * @param channelId
//	 * @param richMenuId
//	 * @param referenceId
//	 * @throws BcsNoticeException 
//	 */
//	public void callUploadImageAPI(String channelId, String richMenuId, String resourceId) throws BcsNoticeException  {
//		logger.info(" ContentRichMenuUIService callUploadImageAPI");
//		try{
//			ContentResource contentResource = contentResourceService.findOne(resourceId);
//			PostLineResponse result = lineRichMenuApiService.callUploadImageAPI(channelId, richMenuId, contentResource.getContentType(), resourceId, 0);
//			if(result.getStatus() != 200){
//				throw new Exception(result.getResponseStr());
//			}
//			logger.debug("callUploadImageAPI result:" + result);			
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			throw new BcsNoticeException(e.getMessage());
//		}
//	}
//	
//	/**
//	 * 呼叫刪除圖文選單API
//	 * 
//	 * @param channelId
//	 * @param richMenuId
//	 * @throws BcsNoticeException 
//	 */
//	public void callDeleteRichMenuAPI(String channelId, String richMenuId) throws BcsNoticeException  {
//		logger.info(" ContentRichMenuUIService callDeleteRichMenuAPI");
//		try{			
//			PostLineResponse result = lineRichMenuApiService.callDeleteRichMenuAPI(channelId, richMenuId, 0);
//			if(result.getStatus() != 200){
//				throw new Exception(result.getResponseStr());
//			}
//			logger.debug("callDeleteRichMenuAPI result:" + result);			
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			throw new BcsNoticeException(e.getMessage());
//		}
//	}

}
