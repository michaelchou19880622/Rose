package com.bcs.web.tracing.controller;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bcs.core.aspect.annotation.WebServiceLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.entity.ShareCampaignClickTracing;
import com.bcs.core.db.entity.ShareUserRecord;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.ShareCampaignClickTracingService;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.db.service.ShareUserRecordService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LineLoginUtil;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.m.controller.MobileUserController;

@Controller
@RequestMapping("/c")
public class MobileMgmClickTracingController extends BCSBaseController {

    @Autowired
    private MobileUserController mobileUserController;
    @Autowired
    private ShareUserRecordService shareUserRecordService;
    @Autowired
    private ShareCampaignClickTracingService shareCampaignClickTracingService;
    @Autowired
    private ShareCampaignService shareCampaignService;
    @Autowired
    private LineUserService lineUserService;

    /** Logger */
    private static Logger logger = Logger.getLogger(MobileMgmClickTracingController.class);

    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/{tracingIdStr}")
    public String startMgmClickTracing(@PathVariable String tracingIdStr, HttpServletRequest request,
            HttpServletResponse response, Model model) throws Exception {
        logger.info("startMgmClickTracing:" + tracingIdStr);

        try {

            LineLoginUtil.addLineoauthLinkInModel(model, UriHelper.getMgmClickOauth(), tracingIdStr);

            return MobilePageEnum.MgmTracingStartPage.toString();

        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return mobileUserController.indexPage(request, response, model);
        }
    }
    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/validate")
    public void validateMgmClickTracing(HttpServletRequest request, HttpServletResponse response, Model model)
            throws Exception {
        logger.info("validateMgmClickTracing");

        try {
            String code = request.getParameter("code");
            logger.info("validateMgmClickTracing code:" + code);

            String state = request.getParameter("state");
            logger.info("validateMgmClickTracing state:" + state);

            String error = request.getParameter("error");
            logger.info("validateMgmClickTracing error:" + error);

            String errorCode = request.getParameter("errorCode");
            logger.info("validateMgmClickTracing errorCode:" + errorCode);

            String errorMessage = request.getParameter("errorMessage");
            logger.info("validateMgmClickTracing errorMessage:" + errorMessage);

            if (StringUtils.isBlank(state)) {
                throw new Exception("TracingId Error:" + state);
            }

            ShareUserRecord ownerRecord = shareUserRecordService.findOne(state);

            if (ownerRecord == null) {
                throw new Exception("TracingId Error:" + state);
            }

//            if (StringUtils.isNotBlank(error) || StringUtils.isNotBlank(errorCode)) {
//
//                String linkUrl = UriHelper.getMgmClickTracingUrl() + state;
//                response.sendRedirect(linkUrl);
//                return;
//            }

            String campaignId = ownerRecord.getCampaignId();
            ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);

            // 活動是否存在
            if(shareCampaign == null || !ShareCampaign.STATUS_ACTIVE.equals(shareCampaign.getStatus())) {
                response.sendRedirect(UriHelper.getMgmRedirectPage(null, "查無此活動"));
                return;
            }

            // 活動是否過期
            Date now = new Date();
            if(!now.after(shareCampaign.getStartTime()) || !now.before(shareCampaign.getEndTime())) {
                response.sendRedirect(UriHelper.getMgmRedirectPage(null, "活動已結束，感謝您的參與"));
                return;
//            	response.sendRedirect(UriHelper.getGoMgmPage(campaignId));
//            	return;
            }

            // 取得UID、好友狀態
            Map<String, String> resultMap = LineLoginUtil.callRetrievingAPI(code, UriHelper.getMgmClickOauth(), state);
            String uid = resultMap.get("UID");
            boolean friendFlag = Boolean.valueOf(resultMap.get("friendFlag"));

            if(StringUtils.isNotBlank(uid)) {
                lineUserService.findByMidAndCreateUnbind(uid);
            }

        	ShareCampaignClickTracing clickTracing = shareCampaignClickTracingService.findByUidAndShareUserRecordId(uid, ownerRecord.getShareUserRecordId());

        	if(friendFlag){ // 好友

        	    if(clickTracing == null && !ownerRecord.getUid().equals(uid)) { //未點過、非本人

        	        clickTracing = new ShareCampaignClickTracing();
        	        clickTracing.setUid(uid);
        	        clickTracing.setShareUserRecordId(ownerRecord.getShareUserRecordId());
        	        clickTracing.setModifyTime(new Date());
        	        shareCampaignClickTracingService.save(clickTracing);
        	    }

                HttpSession session = request.getSession();

                session.setAttribute("MID", uid);
                session.setAttribute("campaignId", campaignId);

                response.sendRedirect(UriHelper.getMgmPage());
                return;
            }
            else { // 非好友
                response.sendRedirect(CoreConfigReader.getString(CONFIG_STR.ADD_LINE_FRIEND_LINK, true));
                return;
            }


        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            String linkUrl = UriHelper.bcsMPage;
            response.sendRedirect(linkUrl);
            return;
        }
    }
    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/m/{tracingIdStr}")
    public String startMgmTracing(@PathVariable String tracingIdStr, HttpServletRequest request,
            HttpServletResponse response, Model model) throws Exception {
        logger.info("startMgmTracing:" + tracingIdStr);

        try {

            LineLoginUtil.addLineoauthLinkInModel(model, UriHelper.getMgmOauth(), tracingIdStr);

            return MobilePageEnum.MgmTracingStartPage.toString();

        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return mobileUserController.indexPage(request, response, model);
        }
    }
    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/m/validate")
    public void validateMgmTracing(HttpServletRequest request, HttpServletResponse response, Model model)
            throws Exception {
        logger.info("validateMgmTracing");

        try {
            String code = request.getParameter("code");
            logger.info("validateMgmTracing code:" + code);

            String state = request.getParameter("state");
            logger.info("validateMgmTracing state:" + state);

            String error = request.getParameter("error");
            logger.info("validateMgmTracing error:" + error);

            String errorCode = request.getParameter("errorCode");
            logger.info("validateMgmTracing errorCode:" + errorCode);

            String errorMessage = request.getParameter("errorMessage");
            logger.info("validateMgmTracing errorMessage:" + errorMessage);

            if (StringUtils.isBlank(state)) {
                throw new Exception("TracingId Error:" + state);
            }

//            if (StringUtils.isNotBlank(error) || StringUtils.isNotBlank(errorCode)) {
//
//                String linkUrl = UriHelper.getMgmTracingUrl() + state;
//                response.sendRedirect(linkUrl);
//                return;
//            }

            // 取得UID、好友狀態
            Map<String, String> resultMap = LineLoginUtil.callRetrievingAPI(code, UriHelper.getMgmOauth(), state);
            String uid = resultMap.get("UID");
            boolean friendFlag = Boolean.valueOf(resultMap.get("friendFlag"));

            if(StringUtils.isNotBlank(uid)) {
                lineUserService.findByMidAndCreateUnbind(uid);
            }

            if(friendFlag){ // 好友

                HttpSession session = request.getSession();

                session.setAttribute("MID", uid);
                session.setAttribute("campaignId", state);

                response.sendRedirect(UriHelper.getMgmPage());
                return;
            }
            else { // 非好友
                response.sendRedirect(CoreConfigReader.getString(CONFIG_STR.ADD_LINE_FRIEND_LINK, true));
                return;
            }

        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            String linkUrl = UriHelper.bcsMPage;
            response.sendRedirect(linkUrl);
            return;
        }
    }
}
