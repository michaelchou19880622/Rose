package com.bcs.web.resource.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.akka.service.AkkaCoreService;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.record.akke.model.ClickLinkModel;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.FileUtil;
import com.bcs.core.utils.UrlUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;


@Controller
@RequestMapping("/bcs")
public class BCSResourceController extends BCSBaseController {

	@Autowired
	private SendGroupService sendGroupService;
	@Autowired
	private AdminUserService adminUserService;
	@Autowired
	private ContentResourceService contentResourceService;
	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private AkkaCoreService akkaCoreService;
	@Autowired
	private SendingMsgService sendingMsgService;

	/** Logger */
	private static Logger logger = Logger.getLogger(BCSResourceController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/getResource/{resourceType}/")
	public void getResource(@PathVariable String resourceType, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("getResource:resourceType:" + resourceType );

		try{
			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType)){
				logger.debug("response default image");
				FileUtil.getFile(response, CoreConfigReader.getString("file.default.image.path"), CoreConfigReader.getString("file.default.image.title"), CoreConfigReader.getString("file.default.image.type"));
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType)){
				logger.debug("response default image");
				FileUtil.getFile(response, CoreConfigReader.getString("file.default.image.path"), CoreConfigReader.getString("file.default.image.title"), CoreConfigReader.getString("file.default.image.type"));
			}
		}
		logger.info("End === getResource:resourceType:" + resourceType );
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getResource/{resourceType}/{resourceId}")
	public void getResource(@PathVariable String resourceType, @PathVariable String resourceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("getResource:resourceType:" + resourceType + ":resourceId:" + resourceId);

		try{
			ContentResource resource = contentResourceService.findOne(resourceId);
			if(resource != null){
				FileUtil.getFile(response, resource);
				logger.info("End === getResource:resourceType:" + resourceType + ":resourceId:" + resourceId);
				return;
			}

			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType)){
				logger.debug("response default image");
				FileUtil.getFile(response, CoreConfigReader.getString("file.default.image.path"), CoreConfigReader.getString("file.default.image.title"), CoreConfigReader.getString("file.default.image.type"));
				return;
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType)){
				logger.debug("response default image");
				FileUtil.getFile(response, CoreConfigReader.getString("file.default.image.path"), CoreConfigReader.getString("file.default.image.title"), CoreConfigReader.getString("file.default.image.type"));
				return;
			}
		}
		logger.info("End Exception === getResource:resourceType:" + resourceType + ":resourceId:" + resourceId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getResource/{resourceType}/{resourcePreview}/{resourceId}")
	public void getResourcePreView(@PathVariable String resourceType, @PathVariable String resourcePreview, @PathVariable String resourceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("getResourcePreView:" + resourceType + ":resourcePreview:" + resourcePreview + ":resourceId:" + resourceId);

		try{
			int imageSize = 1040;
			boolean isDiffSize = false;
			try{
				int size = Integer.parseInt(resourceId);
				if(size > 0 && size <=imageSize ){
					imageSize = size;
				}
				isDiffSize = true;
			}
			catch(Exception e){} //Skip

			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourcePreview)){
				ContentResource resource = contentResourceService.findOne(resourceId);
				if(resource != null){
					FileUtil.getFile(response, resource, true);
					return;
				}
			}
			else if(isDiffSize){
				// Return different Size Image
				ContentResource resource = contentResourceService.findOne(resourcePreview);
				if(resource != null){
					FileUtil.getFile(response, resource, false);
					return;
				}
			}

			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType) || ContentResource.RESOURCE_TYPE_IMAGE.equals(resourcePreview)){
				logger.debug("response default image");
				FileUtil.getFile(response, CoreConfigReader.getString("file.default.image.path"), CoreConfigReader.getString("file.default.image.title"), CoreConfigReader.getString("file.default.image.type"));
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType) || ContentResource.RESOURCE_TYPE_IMAGE.equals(resourcePreview)){
				logger.debug("response default image");
				FileUtil.getFile(response, CoreConfigReader.getString("file.default.image.path"), CoreConfigReader.getString("file.default.image.title"), CoreConfigReader.getString("file.default.image.type"));
			}
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getLink/{linkId}")
	public void getLink(@PathVariable String linkId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("getLink:" + linkId );

		String MID = request.getParameter("MID");
		logger.info("getLink MID:" + MID);
		String replace = request.getParameter("replace");
		logger.info("getLink replace:" + replace);
		String type = request.getParameter("type");
		logger.info("getLink type:" + type);
		String code = request.getParameter("code");
		logger.info("getLink code:" + code);
		String event = request.getParameter("event");
		logger.info("getLink event:" + event);
		String serialId = request.getParameter("serialId");
		logger.info("getLink serialId:" + serialId);
		String time = request.getParameter("time");
		logger.info("getLink time:" + time);
		String hash = request.getParameter("hash");
		logger.info("getLink hash:" + hash);

		if(StringUtils.isBlank(MID)){
			MID = (String) request.getSession().getAttribute("MID");
		}
		else{
			boolean validate = false;

			if(StringUtils.isNotBlank(serialId)){
				validate = UrlUtil.validateHash(MID, serialId, time, hash);
			}
			else if(StringUtils.isNotBlank(replace)){
				String keyword = URLEncoder.encode(replace, "UTF-8");
				validate = UrlUtil.validateHash(MID, keyword, time, hash);
			}
			else if(StringUtils.isNotBlank(type)){
				validate = UrlUtil.validateHash(MID, type, time, hash);
			}
			else{
				validate = UrlUtil.validateHash(MID, null, time, hash);
			}
			if(!validate){
				String linkUrl = UriHelper.bcsMPage;
				linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
				response.sendRedirect(linkUrl);
				return;
			}
			request.getSession().setAttribute("MID", MID);
		}

		if(StringUtils.isNotBlank(MID)){
			// Send Event
			if(StringUtils.isNotBlank(event)){
				sendingMsgService.sendEventMessage(MID, event);
			}

			if(StringUtils.isNotBlank(type)){

				String linkUrl = UriHelper.getIndexToPageUri(MID, type, linkId);

				this.saveLog(MID, linkUrl + "--" + type, linkId);

				// getLink ClickLinkModel
				akkaCoreService.recordMsgs(new ClickLinkModel(linkId, new Date()));
				response.sendRedirect(linkUrl);
				return;
			}
			else{
				ContentLink contentLink = contentLinkService.findOne(linkId);
				if(contentLink != null){
					String linkUrl = contentLink.getLinkUrl();
					if(StringUtils.isNotBlank(linkUrl)){

						linkUrl = UriHelper.parseBcsPage(linkUrl, MID);

						linkUrl = UrlUtil.encodeAndReplace(linkUrl, replace);

                        String encryptedMID = CryptUtil.Encrypt("AES", MID, CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true), CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true));
                        String encodeMID = URLEncoder.encode(encryptedMID, "UTF-8");

                        linkUrl = UrlUtil.encodeAndHash(linkUrl, encodeMID, code);

						linkUrl = UrlUtil.replaceSerialSetting(linkUrl, MID, serialId);

						this.saveLog(MID, linkUrl + "--" + contentLink, contentLink.getLinkId());

						// getLink ClickLinkModel
						akkaCoreService.recordMsgs(new ClickLinkModel(linkId, new Date()));

						logger.debug("getLink linkUrl:" + linkUrl);

						linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
						response.sendRedirect(linkUrl);
						return;
					}
				}
			}

			// Redirect to mobile Default Page
			String linkUrl = UriHelper.getGoIndexUri();
			response.sendRedirect(linkUrl);
			return;
		}
		else{
			String linkUrl = UriHelper.bcsMPage;
			linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
			response.sendRedirect(linkUrl);
			return;
		}
	}

	private void saveLog(String MID, String contentLink, String referenceId){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();

		// ClickLink Log
		UserTraceLog msgs = new UserTraceLog();
		msgs.setTarget(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink);
		msgs.setAction(LOG_TARGET_ACTION_TYPE.ACTION_ClickLink);
		msgs.setModifyTime(now);
		msgs.setModifyUser(MID);
		msgs.setLevel(UserTraceLog.USER_TRACE_LOG_LEVEL_TRACE);
		msgs.setModifyDay(sdf.format(now));

		msgs.setContent(contentLink);
		msgs.setReferenceId(referenceId);
		ApplicationContextProvider.getApplicationContext().getBean(UserTraceLogService.class).bulkPersist(msgs);
//		akkaCoreService.recordMsgs(msgs);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/edit/createResource")
	@ResponseBody
	public ResponseEntity<?> createResource(@RequestPart MultipartFile filePart,
			@CurrentUser CustomUser customUser,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("createResource");

		try{
			if(filePart != null){
				String resourceType = request.getParameter("resourceType");
				logger.debug("resourceType:" + resourceType);

				logger.debug("getOriginalFilename:" + filePart.getOriginalFilename());
				logger.debug("getContentType:" + filePart.getContentType());
				logger.debug("getSize:" + filePart.getSize());

				String modifyUser = customUser.getAccount();
				logger.debug("modifyUser:" + modifyUser);

				ContentResource resource = contentResourceService.uploadFile(filePart, resourceType, modifyUser);

				return new ResponseEntity<>(resource, HttpStatus.OK);
			}
			else{
				throw new Exception("Create Resource Null");
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
}
