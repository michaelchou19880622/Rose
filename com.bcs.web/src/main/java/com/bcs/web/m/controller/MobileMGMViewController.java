package com.bcs.web.m.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.entity.ShareUserRecord;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.db.service.ShareUserRecordService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.m.service.MobilePageService;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;

@Controller
@RequestMapping("/m")
public class MobileMGMViewController extends BCSBaseController {

    @Autowired
    private ShareUserRecordService shareUserRecordService;
    @Autowired
    private ShareCampaignService shareCampaignService;
    @Autowired
    private MobilePageService mobilePageService;
    @Autowired
    private LineUserService lineUserService;

    /** Logger */
    private static Logger logger = Logger.getLogger(MobileMGMViewController.class);

    @RequestMapping(method = RequestMethod.GET, value = "/goMgmPage")
    public void goMgmPage(HttpServletRequest request, HttpServletResponse response) throws IOException{
        logger.info("goMgmPage");
        String MID = request.getParameter("MID");
        String campaignId = request.getParameter("campaignId");
        logger.info("goMgmPage campaignId:" + campaignId);

        HttpSession session = request.getSession();
        
        if(StringUtils.isBlank(MID)){
            MID = (String) session.getAttribute("MID");
        }
        if(StringUtils.isBlank(MID)){
          String linkUrl = UriHelper.bcsMPage;
          response.sendRedirect(linkUrl);
          return;
        }
        
        if(StringUtils.isBlank(campaignId)){
            campaignId = (String) session.getAttribute("campaignId");
        }
        if(StringUtils.isBlank(campaignId)){
          String linkUrl = UriHelper.bcsMPage;
          response.sendRedirect(linkUrl);
          return;
        }

        session.setAttribute("MID", MID);
        session.setAttribute("campaignId", campaignId);
        
        response.sendRedirect(UriHelper.getMgmPage());
        return;
    }
    
    /**
     * MGM Page
     * 
     * @param model
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/mgmPage")
    public String mgmPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.info("mgmPage");

        HttpSession session = request.getSession();
        ShareCampaign shareCampaign = null;
        try {   
            String MID = null;
            String campaignId = null;
            
            if (session.getAttribute("MID") != null) {
                MID = (String) session.getAttribute("MID");
            }
            if (StringUtils.isBlank(MID)) {
                throw new Exception("MID null");
            }
            if (session.getAttribute("campaignId") != null) {
                campaignId = (String) session.getAttribute("campaignId");
            }
            if (StringUtils.isBlank(campaignId)) {
                throw new Exception("CampaignId null");
            }
                        
            shareCampaign = shareCampaignService.findOne(campaignId);
            
            if(shareCampaign == null || !ShareCampaign.STATUS_ACTIVE.equals(shareCampaign.getStatus())) {
                model.addAttribute("msg", "查無此活動");
                model.addAttribute("replaceLink", UriHelper.bcsMPage);
                return MobilePageEnum.MgmRedirectPage.toString();
            }

            // 活動是否過期
            Date now = new Date();
            if(!now.after(shareCampaign.getStartTime()) || !now.before(shareCampaign.getEndTime())) {
                model.addAttribute("msg", "活動已結束，感謝您的參與");
                model.addAttribute("replaceLink", UriHelper.bcsMPage);
                return MobilePageEnum.MgmRedirectPage.toString();
            }
            
            LineUser user = lineUserService.findByMid(MID);
            
            //如未加好友
            if(user == null || LineUser.STATUS_BLOCK.equals(user.getStatus())) {
                model.addAttribute("replaceLink", CoreConfigReader.getString(CONFIG_STR.ADD_LINE_FRIEND_LINK, true));
                return MobilePageEnum.MgmRedirectPage.toString();
            }

            mobilePageService.visitPageLog(MID, campaignId, "mgmPage");
            
//          String actionImgUrl = CoreConfigReader.getString(CONFIG_STR.MGM_ACTION_IMG_CDN_URL, true);
//          String shareImgUrl = CoreConfigReader.getString(CONFIG_STR.MGM_SHARE_IMG_CDN_URL, true);
//          String descriptionImgUrl = CoreConfigReader.getString(CONFIG_STR.MGM_DESCRIPTION_IMG_CDN_URL, true);
          
//          if(StringUtils.isBlank(actionImgUrl) || StringUtils.isBlank(shareImgUrl) || StringUtils.isBlank(descriptionImgUrl)) {
          String actionImgUrl = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, shareCampaign.getActionImgReferenceId());
          //actionImgUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, shareCampaign.getActionImgReferenceId());
          String shareImgUrl = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, shareCampaign.getShareImgReferenceId());
          //shareImgUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, shareCampaign.getShareImgReferenceId());
          String descriptionImgUrl = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, shareCampaign.getDescriptionImgReferenceId());
          //descriptionImgUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, shareCampaign.getDescriptionImgReferenceId());
//          }
            
            model.addAttribute("actionImgUrl", actionImgUrl);
            model.addAttribute("shareImgUrl", shareImgUrl);
            model.addAttribute("descriptionImgUrl", descriptionImgUrl);
            return MobilePageEnum.DoMgmPage.toString();//TODO 
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));

        	model.addAttribute("replaceLink", UriHelper.bcsMPage);
            return MobilePageEnum.MgmRedirectPage.toString();
        }
    }

    // 分享
    @RequestMapping(method = RequestMethod.POST, value = "/mgm/doMgmSharing")
    @ResponseBody
    public String doMgmSharing(Model model, HttpServletRequest request,
            HttpServletResponse response) {
        logger.info("doMgmSharing");

        HttpSession session = request.getSession();

        try {
            String uid = (String) session.getAttribute("MID");
            String campaignId = (String) session.getAttribute("campaignId");
            
            ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);
            
            if(shareCampaign == null || !ShareCampaign.STATUS_ACTIVE.equals(shareCampaign.getStatus())) {
                throw new Exception("CampaignId Error :" + campaignId);
            }
            
            Date now = new Date();
            if(!now.after(shareCampaign.getStartTime()) || !now.before(shareCampaign.getEndTime())) {
                throw new Exception("Not in the term of validity");
            }
            
            ShareUserRecord shareUserRecord = shareUserRecordService.findByCampaignIdAndUid(campaignId, uid);

            if (shareUserRecord == null) {
                
                shareUserRecord = new ShareUserRecord();
                shareUserRecord.setShareUserRecordId(shareUserRecordService.generateShareUserRecordId());
                shareUserRecord.setUid(uid);
                shareUserRecord.setModifyTime(now);
                shareUserRecord.setCampaignId(campaignId);
                shareUserRecordService.save(shareUserRecord);
            }

            UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ShareUserRecord, LOG_TARGET_ACTION_TYPE.ACTION_DoMgmSharing, uid, shareUserRecord, shareUserRecord.getShareUserRecordId());
            
            String message = null;
            if(StringUtils.isNotBlank(shareCampaign.getShareMsg())) {
                message = shareCampaign.getShareMsg() + "\n";
            }
            String url = UriHelper.getMgmClickTracingUrl() + shareUserRecord.getShareUserRecordId();
 
            return URLEncoder.encode(message + url, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return "error";
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/mgmRedirectPage")
    public String mgmRedirectPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.info("mgmRedirectPage");

        try {
            String replaceLink = request.getParameter("replaceLink");
            String msg = request.getParameter("msg");
            
            if(StringUtils.isNotBlank(replaceLink)) {
                model.addAttribute("replaceLink", replaceLink);
            }
            else {
                model.addAttribute("replaceLink", UriHelper.bcsMPage);
            }
            
            if(StringUtils.isNotBlank(msg)) {
                model.addAttribute("msg", msg);
            }
            
            return MobilePageEnum.MgmRedirectPage.toString();
            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));

            model.addAttribute("replaceLink", UriHelper.bcsMPage);
            return MobilePageEnum.MgmRedirectPage.toString();
        }
    }
}
