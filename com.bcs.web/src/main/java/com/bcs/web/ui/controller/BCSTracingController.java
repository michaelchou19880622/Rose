package com.bcs.web.ui.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bcs.core.aspect.annotation.WebServiceLog;
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

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentLinkTracing;
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
import com.bcs.web.ui.model.LinkClickReportSearchModel;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.bcs.web.ui.model.TracingLinkListModel;
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
	private ContentFlagService contentFlagService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSTracingController.class);

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/tracingGeneratePage")
	public String tracingGeneratePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("tracingGeneratePage");
		return BcsPageEnum.TracingGeneratePage.toString();
	}

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/tracingGenerateListPage")
	public String tracingGenerateListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("tracingGenerateListPage");
		return BcsPageEnum.TracingGenerateListPage.toString();
	}

	@WebServiceLog
	@ControllerLog(description="getTracingLinkList")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/getTracingLinkList", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getTracingLinkList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody LinkClickReportSearchModel linkClickReportSearchModel) throws IOException {
		String queryFlag = linkClickReportSearchModel.getQueryFlag() == null ? "" : new String(linkClickReportSearchModel.getQueryFlag().getBytes("utf-8"),"utf-8");
		Integer page = linkClickReportSearchModel.getPage() == null ? 0 : linkClickReportSearchModel.getPage();
		int pageSize = linkClickReportSearchModel.getPageSize() == null ? 20 : linkClickReportSearchModel.getPageSize();
		logger.info("getTracingLinkList start, page=" + page + " pageSize=" + pageSize + " queryFlag=" + queryFlag);
		Map<String, Object> result = new HashMap<String, Object>();
		try{
			List<Object[]> listObj = contentLinkTracingService.findListByFlag(queryFlag, page * pageSize + 1, pageSize);
			List<TracingLinkListModel> list = new ArrayList<TracingLinkListModel>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			for(Object[] data : listObj){
				TracingLinkListModel tracingLinkListModel = new TracingLinkListModel();
				ContentLinkTracing item = new ContentLinkTracing();
				item.setTracingId(Long.parseLong(castToString(data[0])));
				item.setLinkId(castToString(data[1]));
				item.setLinkIdBinded(castToString(data[2]));
				item.setLinkIdUnMobile(castToString(data[3]));
				item.setModifyTime(sdf.parse(castToString(data[4])));
				item.setModifyUser(castToString(data[5]));
				tracingLinkListModel.setContentLinkTracing(item);
				ContentLink linkForUnbind = contentLinkService.findOne(item.getLinkId());
				List<String> flagList = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(item.getLinkId(), "LINK");
				linkForUnbind.setLinkTag(getFlagListString(flagList));
				tracingLinkListModel.setContentLinkUnbind(linkForUnbind);
				ContentLink linkForBind = contentLinkService.findOne(item.getLinkIdBinded());
				flagList = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(item.getLinkIdBinded(), "LINK");
				linkForBind.setLinkTag(getFlagListString(flagList));
				tracingLinkListModel.setContentLinkBind(linkForBind);
				ContentLink linkForUnmobile = contentLinkService.findOne(item.getLinkIdUnMobile());
				flagList = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(item.getLinkIdUnMobile(), "LINK");
				linkForUnmobile.setLinkTag(getFlagListString(flagList));
				tracingLinkListModel.setContentLinkUnmobile(linkForUnmobile);
				list.add(tracingLinkListModel);
			}
			result.put("ContentLinkTracingList", list);
			String tracingUrlPre = UriHelper.getTracingUrlPre();
			result.put("TracingUrlPre", tracingUrlPre);
			logger.info("getTracingLinkList end, page=" + page + " pageSize=" + pageSize + " queryFlag=" + queryFlag + " tracingUrlPre=" + tracingUrlPre + " sizeOfList=" + (list == null ? 0: list.size()));
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@WebServiceLog
	@RequestMapping(method = RequestMethod.POST, value ="/edit/tracingGenerate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> tracingGenerate(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestBody TracingLinkModel tracingLinkModel) throws IOException {
		String tracingIdStr = tracingLinkModel == null ? "" : tracingLinkModel.getTracingId();
		logger.info("tracingGenerate start, tracingId=" + tracingIdStr);
		try{
			String adminUserAccount = customUser.getAccount();
			if(tracingLinkModel != null){
				List<SendMsgDetailModel> linkDatas = tracingLinkModel.getLinkData();
				if(linkDatas != null && linkDatas.size() == 3){
					String tracingUrl;
					if(StringUtils.isNotBlank(tracingIdStr)){
						Long tracingId = Long.parseLong(tracingIdStr);
						contentLinkTracingUIService.generateTracingLink(tracingId, linkDatas.get(0), linkDatas.get(1), linkDatas.get(2), adminUserAccount);
						tracingUrl = UriHelper.getTracingUrl(tracingId);
						logger.info("tracingGenerate start, tracingId=" + tracingIdStr + " tracingUrl=" + tracingUrl);
						return new ResponseEntity<>(tracingUrl, HttpStatus.OK);
					}
					else{
						Long result = contentLinkTracingUIService.generateTracingLink(linkDatas.get(0), linkDatas.get(1), linkDatas.get(2), adminUserAccount);
						tracingUrl = UriHelper.getTracingUrl(result);
						logger.info("tracingGenerate start, tracingId=" + tracingIdStr + " tracingUrl=" + tracingUrl);
						return new ResponseEntity<>(tracingUrl, HttpStatus.OK);
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

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getTracingLinkData")
	public ResponseEntity<?> getTracingLinkData(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestParam Long tracingId) {
		logger.info("getTracingLinkData start, tracingId=" + tracingId);
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
						logger.info("getTracingLinkData end, tracingId=" + tracingId);
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
	
	private String castToString(Object obj){
		if(obj != null){
			return obj.toString();
		}
		return "";
	}
	
	private String getFlagListString(List<String> flagList){
		String flagListString = "";
		for (String tag : flagList) {
		   	flagListString += tag + ";";
 	    }
		return flagListString;
	}
}
