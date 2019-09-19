package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.ContentLinkTracingService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.bcs.web.ui.model.TracingLinkModel;
import com.bcs.web.ui.service.ContentLinkTracingUIService;


@Controller
@RequestMapping("/bcs")
public class BCSTracingController extends BCSBaseController {
	@Autowired
	private ContentLinkTracingUIService contentLinkTracingUIService;
	@Autowired
	private ContentLinkTracingService contentLinkTracingService;
	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private AdminUserService adminUserService;
	@Autowired
	private ContentFlagService contentFlagService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSTracingController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/tracingGeneratePage")
	public String tracingGeneratePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("tracingGeneratePage");
		return BcsPageEnum.TracingGeneratePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/tracingGenerateListPage")
	public String tracingGenerateListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("tracingGenerateListPage");
		return BcsPageEnum.TracingGenerateListPage.toString();
	}
	
	@ControllerLog(description="getTracingLinkList")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getTracingLinkList")
	public ResponseEntity<?> getTracingLinkList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getTracingLinkList");
		
		Map<String, Object> result = new HashMap<String, Object>();
		List<ContentLinkTracing> list = contentLinkTracingService.findAll();
		
		result.put("ContentLinkTracingList", list);

		// Get Link Detail
		Map<String, ContentLink> links =new HashMap<String, ContentLink>();
		
		List<String> linkIds = new ArrayList<String>();
		for(int i = 1; i <= list.size(); i++){
			
			linkIds.add(list.get(i-1).getLinkId());
			
			if(i % 1000 == 0){
				List<ContentLink> contentLinkResult = contentLinkService.findByLinkIdIn(linkIds);
				if(contentLinkResult != null && contentLinkResult.size() > 0){
					for(ContentLink link : contentLinkResult){
						links.put(link.getLinkId(), link);
					}
				}
				linkIds.clear();
			}
		}
		
		if(linkIds.size() > 0){
			List<ContentLink> contentLinkResult = contentLinkService.findByLinkIdIn(linkIds);
			if(contentLinkResult != null && contentLinkResult.size() > 0){
				for(ContentLink link : contentLinkResult){
					links.put(link.getLinkId(), link);
				}
			}
		}
		
		result.put("ContentLinkList", links);
		
		// Get Link Flags
		Map<String, List<String>> FlagList =new HashMap<String, List<String>>();
		for(String linkId : links.keySet()){
			List<String> flags = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(linkId, ContentFlag.CONTENT_TYPE_LINK);
			FlagList.put(linkId, flags);
		}
		result.put("FlagList", FlagList);
		
		String TracingUrlPre = UriHelper.getTracingUrlPre();
		result.put("TracingUrlPre", TracingUrlPre);

		/**
		 * AdminUser Result Map
		 */
		try{
			Map<String, AdminUser> admins = adminUserService.findAllMap();
			Map<String, String> adminMap = new HashMap<String, String>();
			for(ContentLinkTracing contentLinkTracing : list){
				String userAccount = contentLinkTracing.getModifyUser();
				if(admins.containsKey(userAccount)){
					adminMap.put(userAccount, admins.get(userAccount).getUserName());
				}
			}
			result.put("AdminUser", adminMap);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ControllerLog(description="tracingGenerate")
	@RequestMapping(method = RequestMethod.POST, value ="/edit/tracingGenerate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> tracingGenerate(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestBody TracingLinkModel tracingLinkModel) throws IOException {
		logger.info("tracingGenerate");

		try{
			String adminUserAccount = customUser.getAccount();
			
			if(tracingLinkModel != null){
				List<SendMsgDetailModel> linkDatas = tracingLinkModel.getLinkData();
				if(linkDatas != null && linkDatas.size() == 3){
					
					String tracingIdStr = tracingLinkModel.getTracingId();

					if(StringUtils.isNotBlank(tracingIdStr)){
						Long tracingId = Long.parseLong(tracingIdStr);
					
						contentLinkTracingUIService.generateTracingLink(tracingId, linkDatas.get(0), linkDatas.get(1), linkDatas.get(2), adminUserAccount);

						return new ResponseEntity<>(UriHelper.getTracingUrl(tracingId), HttpStatus.OK);
					}
					else{
					
						Long result = contentLinkTracingUIService.generateTracingLink(linkDatas.get(0), linkDatas.get(1), linkDatas.get(2), adminUserAccount);
						
						return new ResponseEntity<>(UriHelper.getTracingUrl(result), HttpStatus.OK);
					}
				}
				else{
					throw new BcsNoticeException("請輸入追蹤聯結");
				}
			}
			else{
				throw new Exception("TracingLinkModel Null");
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description="getTracingLinkData")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getTracingLinkData")
	public ResponseEntity<?> getTracingLinkData(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestParam Long tracingId) {
		logger.info("getTracingLinkData");

		try{
			Map<String, Object> result = new HashMap<String, Object>();
			
			if(tracingId != null){
				logger.info("tracingId:" + tracingId);
				ContentLinkTracing contentLinkTracing = contentLinkTracingService.findOne(tracingId);
				
				if(contentLinkTracing != null){
					result.put("ContentLinkTracing", contentLinkTracing);
					
					ContentLink contentLink = contentLinkService.findOne(contentLinkTracing.getLinkId());

					ContentLink contentLinkBinded = null;
					String linkIdBinded = contentLinkTracing.getLinkIdBinded();
					if(StringUtils.isNotBlank(linkIdBinded)){
						contentLinkBinded = contentLinkService.findOne(linkIdBinded);
					}

					ContentLink contentLinkUnMobile = null;
					String linkIdUnMobile = contentLinkTracing.getLinkIdUnMobile();
					if(StringUtils.isNotBlank(linkIdUnMobile)){
						contentLinkUnMobile = contentLinkService.findOne(linkIdUnMobile);
					}

					if(contentLink != null){
						result.put("ContentLink", contentLink);
						result.put("ContentLinkBinded", contentLinkBinded);
						result.put("ContentLinkUnMobile", contentLinkUnMobile);
						
						String TracingUrlPre = UriHelper.getTracingUrlPre();
						result.put("TracingUrlPre", TracingUrlPre);
						
						return new ResponseEntity<>(result, HttpStatus.OK);
					}
				}
			}
			
			throw new Exception("TracingId Null");
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
