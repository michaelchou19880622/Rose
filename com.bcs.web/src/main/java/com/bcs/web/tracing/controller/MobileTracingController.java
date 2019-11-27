package com.bcs.web.tracing.controller;

import com.bcs.core.api.service.LineProfileService;
import com.bcs.core.api.service.LineWebLoginApiService;
import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.ContentLinkTracingService;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.UrlUtil;
import com.bcs.core.validate.service.UserValidateService;
import com.bcs.core.web.m.service.MobilePageService;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.m.controller.MobileUserController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

// import com.bcs.core.akka.service.AkkaCoreService;
// import com.bcs.core.record.akke.model.WebLoginClickLinkModel;
// import com.bcs.core.servlet.service.HttpSessionService;


@Controller
@RequestMapping("/l")
public class MobileTracingController extends BCSBaseController {
    @Autowired
    private ContentLinkService contentLinkService;
    /* @Autowired
    private AkkaCoreService akkaCoreService; */
    @Autowired
    private MobileUserController mobileUserController;
    @Autowired
    private ContentLinkTracingService contentLinkTracingService;
    @Autowired
    private LineWebLoginApiService lineWebLoginApiService;
    @Autowired
    private MobilePageService mobilePageService;
    /* @Autowired
    private HttpSessionService httpSessionService; */
    @Autowired
    private LineUserService lineUserService;
    @Autowired
    private UserValidateService userValidateService;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(MobileTracingController.class);

    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/{tracingIdStr}")
    public String startTracing(@PathVariable String tracingIdStr,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Model model) throws Exception {
        logger.info("startTracing:" + tracingIdStr);

        try {
            String code = request.getParameter("code");
            logger.info("startTracing code:" + code);

            String event = request.getParameter("event");
            logger.info("startTracing event:" + event);

            Long tracingId = Long.parseLong(tracingIdStr);
            String sessionMID = (String) request.getSession().getAttribute("MID");
            logger.info("sessionMID:" + sessionMID);

            ContentLinkTracing contentLinkTracing = contentLinkTracingService.findOne(tracingId);

            if (contentLinkTracing == null) {
                throw new Exception("TracingId Error:" + tracingId);
            }

            String linkId = contentLinkTracing.getLinkId();
            if (StringUtils.isBlank(linkId)) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkId:" + linkId);
            }

            String linkIdBinded = contentLinkTracing.getLinkIdBinded();
            if (StringUtils.isBlank(linkIdBinded)) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdBinded:" + linkIdBinded);
            }

            String linkIdUnMobile = contentLinkTracing.getLinkIdUnMobile();
            if (StringUtils.isBlank(linkIdUnMobile)) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdUnMobile:" + linkIdUnMobile);
            }

            ContentLink contentLink = contentLinkService.findOne(linkId);

            if (contentLink == null) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkId:" + linkId);
            }

            ContentLink contentLinkBinded = contentLinkService.findOne(linkIdBinded);

            if (contentLinkBinded == null) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdBinded:" + linkIdBinded);
            }

            ContentLink contentLinkUnMobile = contentLinkService.findOne(linkIdUnMobile);

            if (contentLinkUnMobile == null) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdUnMobile:" + linkIdUnMobile);
            }

            String lineoauthLink = "";
            String bcsTargetLink = "";

            boolean isGetFromSession = CoreConfigReader.getBoolean(CONFIG_STR.TRACING_CONFIG_GET_FROM_SESSION, true);
            boolean useSwitch = CoreConfigReader.getBoolean(CONFIG_STR.TRACING_CONFIG_USE_SWITCH, true);
            boolean checkMobile = CoreConfigReader.getBoolean(CONFIG_STR.TRACING_CONFIG_CHECK_MOBILE, true);

            if (StringUtils.isNotBlank(sessionMID) && isGetFromSession) {
                boolean isbinded = userValidateService.isBinding(sessionMID);
                if (isbinded) {
                    lineoauthLink = UriHelper.getLinkUriCode(linkIdBinded, code, event);
                    bcsTargetLink = UriHelper.getLinkUriCode(linkIdBinded, code, event);
                } else {
                    lineoauthLink = UriHelper.getLinkUriCode(linkId, code, event);
                    bcsTargetLink = UriHelper.getLinkUriCode(linkId, code, event);
                }
            } else {

                if (useSwitch) {
                    String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelID.toString(), true);

                    lineoauthLink = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_V2_1);
                    lineoauthLink = lineoauthLink.replace("{ChannelID}", ChannelID);
                    lineoauthLink = lineoauthLink.replace("{RedirectUrl}", URLEncoder.encode(UriHelper.getOauthUrl(), "UTF-8"));

                    String TracingIdStr = tracingId.toString();
                    if (StringUtils.isNotBlank(code)) {
                        TracingIdStr += ":" + code;
                    }

                    if (StringUtils.isNotBlank(event)) {
                        TracingIdStr += ";" + event;
                    }

                    lineoauthLink = lineoauthLink.replace("{TracingId}", TracingIdStr);
                } else {

                    lineoauthLink = this.getTargetUrl(contentLink, contentLinkBinded, sessionMID, true);

                    if (UriHelper.checkWithMidReplace(lineoauthLink)) {
                        lineoauthLink = this.getTargetUrl(contentLinkUnMobile);

                        if (UriHelper.checkWithMidReplace(lineoauthLink)) {
                            lineoauthLink = UriHelper.bcsMPage;
                        }
                    }
                }

                bcsTargetLink = this.getTargetUrl(contentLinkUnMobile);

                if (UriHelper.checkWithMidReplace(bcsTargetLink)) {
                    bcsTargetLink = UriHelper.bcsMPage;
                }
            }

            model.addAttribute("lineoauthLink", lineoauthLink);
            if (!checkMobile) {
                model.addAttribute("bcsTargetLink", lineoauthLink);
                logger.info("bcsTargetLink:" + lineoauthLink);
            } else {
                model.addAttribute("bcsTargetLink", bcsTargetLink);
                logger.info("bcsTargetLink:" + bcsTargetLink);
            }

            mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserTracingStartPage.getName(), "tracing");
            return MobilePageEnum.UserTracingStartPage.toString();

        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            return mobileUserController.indexPage(request, response, model);
        }
    }

    private String getTargetUrl(ContentLink contentLink, ContentLink contentLinkBinded, String sessionMID, boolean notBcsPage) {
        boolean isbinded = userValidateService.isBinding(sessionMID);

        if (isbinded) {
            String linkUrl = contentLinkBinded.getLinkUrl();
            boolean isBcsPage = UriHelper.checkIsBcsPage(linkUrl);

            if (isBcsPage && notBcsPage) {
                return UriHelper.bcsMPage;
            } else {
                return linkUrl;
            }
        } else {
            String linkUrl = contentLink.getLinkUrl();
            boolean isBcsPage = UriHelper.checkIsBcsPage(linkUrl);

            if (isBcsPage && notBcsPage) {
                return UriHelper.bcsMPage;
            } else {
                return linkUrl;
            }
        }
    }

    private String getTargetUrl(ContentLink contentLinkUnMobile) {
        String linkUrl = contentLinkUnMobile.getLinkUrl();
        boolean isBcsPage = UriHelper.checkIsBcsPage(linkUrl);

        if (isBcsPage) {
            return UriHelper.bcsMPage;
        } else {
            return linkUrl;
        }
    }

    @WebServiceLog
    @RequestMapping(method = RequestMethod.GET, value = "/validate")
    public void validateTracing(HttpServletRequest request,
                                HttpServletResponse response,
                                Model model) throws Exception {
        logger.info("validateTracing");

        try {
            String code = request.getParameter("code");
            logger.info("validateTracing code:" + code);

            String state = request.getParameter("state");
            logger.info("validateTracing state:" + state);

            String error = request.getParameter("error");
            logger.info("validateTracing error:" + error);

            String errorCode = request.getParameter("errorCode");
            logger.info("validateTracing errorCode:" + errorCode);

            String errorMessage = request.getParameter("errorMessage");
            logger.info("validateTracing errorMessage:" + errorMessage);

            String sessionMID = (String) request.getSession().getAttribute("MID");
            logger.info("sessionMID:" + sessionMID);

            if (StringUtils.isBlank(state)) {
                throw new Exception("TracingId Error:" + state);
            }

            Long tracingId = -1L;
            String SendCode = null;
            String SendEvent = null;

            if (state.indexOf(":") > 0 && state.indexOf(";") > 0) {
                String tracingIdStr = state.substring(0, state.indexOf(":"));

                tracingId = Long.parseLong(tracingIdStr);

                SendCode = state.substring(state.indexOf(":") + 1, state.indexOf(";"));

                SendEvent = state.substring(state.indexOf(";") + 1);
            } else if (state.indexOf(":") > 0) {
                String tracingIdStr = state.substring(0, state.indexOf(":"));

                tracingId = Long.parseLong(tracingIdStr);

                SendCode = state.substring(state.indexOf(":") + 1);
            } else if (state.indexOf(";") > 0) {
                String tracingIdStr = state.substring(0, state.indexOf(";"));

                tracingId = Long.parseLong(tracingIdStr);

                SendEvent = state.substring(state.indexOf(";") + 1);
            } else {
                tracingId = Long.parseLong(state);
            }

            ContentLinkTracing contentLinkTracing = contentLinkTracingService.findOne(tracingId);

            if (contentLinkTracing == null) {
                throw new Exception("TracingId Error:" + tracingId);
            }

            String linkId = contentLinkTracing.getLinkId();
            if (StringUtils.isBlank(linkId)) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkId:" + linkId);
            }

            String linkIdBinded = contentLinkTracing.getLinkIdBinded();
            if (StringUtils.isBlank(linkIdBinded)) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdBinded:" + linkIdBinded);
            }

            String linkIdUnMobile = contentLinkTracing.getLinkIdUnMobile();
            if (StringUtils.isBlank(linkIdUnMobile)) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdUnMobile:" + linkIdUnMobile);
            }

            ContentLink contentLink = contentLinkService.findOne(linkId);

            if (contentLink == null) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkId:" + linkId);
            }

            ContentLink contentLinkBinded = contentLinkService.findOne(linkIdBinded);

            if (contentLinkBinded == null) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdBinded:" + linkIdBinded);
            }

            ContentLink contentLinkUnMobile = contentLinkService.findOne(linkIdUnMobile);

            if (contentLinkUnMobile == null) {
                throw new Exception("TracingId Error:" + tracingId + ", LinkIdUnMobile:" + linkIdUnMobile);
            }

            boolean isGetFromSession = CoreConfigReader.getBoolean(CONFIG_STR.TRACING_CONFIG_GET_FROM_SESSION, true);
            boolean useSwitch = CoreConfigReader.getBoolean(CONFIG_STR.TRACING_CONFIG_USE_SWITCH, true);

            if (StringUtils.isNotBlank(sessionMID) && isGetFromSession) {
                boolean isbinded = userValidateService.isBinding(sessionMID);
                if (isbinded) {
                    String linkUrl = UriHelper.getLinkUriCode(linkIdBinded, SendCode, SendEvent);
                    response.sendRedirect(linkUrl);
                } else {
                    String linkUrl = UriHelper.getLinkUriCode(linkId, SendCode, SendEvent);
                    response.sendRedirect(linkUrl);
                }

                return;
            }

            if (StringUtils.isNotBlank(error) || StringUtils.isNotBlank(errorCode)) {
                Map<String, String> result = new HashMap<String, String>();
                result.put("error", error);
                result.put("code", code);
                result.put("state", state);
                result.put("errorCode", errorCode);
                result.put("errorMessage", errorMessage);
                SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_CancelLogin, "SYSTEM", result, state);

                String linkUrl = this.getTargetUrl(contentLink, contentLinkBinded, sessionMID, true);

                if (UriHelper.checkWithMidReplace(linkUrl)) {
                    linkUrl = this.getTargetUrl(contentLinkUnMobile);

                    if (UriHelper.checkWithMidReplace(linkUrl)) {
                        throw new Exception("TracingId linkUrl:" + linkUrl);
                    }
                }

                linkUrl = UrlUtil.encodeAndReplace(linkUrl);
                linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
                response.sendRedirect(linkUrl);
                return;
            } else if (useSwitch) {

                String targetUrl = this.getTargetUrl(contentLink, contentLinkBinded, sessionMID, false);
                boolean isBcsPage = UriHelper.checkIsBcsPage(targetUrl);

                if (isBcsPage) {
                    this.callRetrievingAPI(code, sessionMID, request, response, null, null, contentLink, contentLinkBinded, contentLinkUnMobile, state);
                    return;
                } else {
                    this.callRetrievingAPI(code, sessionMID, request, response, SendCode, SendEvent, contentLink, contentLinkBinded, contentLinkUnMobile, state);
                    return;
                }
            } else {
                String linkUrl = this.getTargetUrl(contentLink, contentLinkBinded, sessionMID, true);

                if (UriHelper.checkWithMidReplace(linkUrl)) {
                    linkUrl = this.getTargetUrl(contentLinkUnMobile);

                    if (UriHelper.checkWithMidReplace(linkUrl)) {
                        linkUrl = UriHelper.bcsMPage;
                    }
                }

                linkUrl = UrlUtil.encodeAndReplace(linkUrl);
                linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
                response.sendRedirect(linkUrl);
                return;
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
        String linkUrl = UriHelper.bcsMPage;
        linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
        response.sendRedirect(linkUrl);
        return;
    }

    private void callRetrievingAPI(String code,
                                   String sessionMID,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   String SendCode,
                                   String SendEvent,
                                   ContentLink contentLink,
                                   ContentLink contentLinkBinded,
                                   ContentLink contentLinkUnMobile,
                                   String state) throws Exception {

        String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelID.toString(), true);
        String ChannelSecret = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelSecret.toString(), true);

        ObjectNode result = lineWebLoginApiService.callRetrievingAPI(ChannelID, ChannelSecret, code, UriHelper.getOauthUrl());

        if (result != null && result.get("access_token") != null) {
            String access_token = result.get("access_token").asText();
            if (StringUtils.isNotBlank(access_token)) {

                ObjectNode getProfile = ApplicationContextProvider.getApplicationContext().getBean(LineProfileService.class).callGetProfileAPI(access_token);

                if (getProfile != null && getProfile.get("userId") != null && StringUtils.isNotBlank(getProfile.get("userId").asText())) {
                    sessionMID = getProfile.get("userId").asText();

                    request.getSession().setAttribute("MID", sessionMID);
                    boolean isbinded = userValidateService.isBinding(sessionMID);

                    if (isbinded) {
                        String linkUrl = UriHelper.getLinkUriCode(contentLinkBinded.getLinkId(), SendCode, SendEvent);

                        lineUserService.findByMidAndCreateUnbind(sessionMID);

                        UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLinkWebLogin, sessionMID, linkUrl + "--" + contentLinkBinded, contentLinkBinded.getLinkId() + ":WebLogin:" + state);
                        UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLinkWebLogin_API, sessionMID, result, contentLinkBinded.getLinkId() + ":WebLogin:" + state);

                        response.sendRedirect(linkUrl);
                    } else {
                        String linkUrl = UriHelper.getLinkUriCode(contentLink.getLinkId(), SendCode, SendEvent);

                        lineUserService.findByMidAndCreateUnbind(sessionMID);

                        UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLinkWebLogin, sessionMID, linkUrl + "--" + contentLink, contentLink.getLinkId() + ":WebLogin:" + state);
                        UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLinkWebLogin_API, sessionMID, result, contentLink.getLinkId() + ":WebLogin:" + state);

                        response.sendRedirect(linkUrl);
                    }
                    return;
                } else {
                    result.put("getProfile", getProfile.toString());
                }
            }
        }

        result.put("code", code);
        SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ValidateLoginApi, "SYSTEM", result, state);

        String linkUrl = this.getTargetUrl(contentLink, contentLinkBinded, sessionMID, false);


        if (UriHelper.checkWithMidReplace(linkUrl)) {
            linkUrl = this.getTargetUrl(contentLinkUnMobile);

            if (UriHelper.checkWithMidReplace(linkUrl)) {
                throw new Exception("TracingId linkUrl:" + linkUrl);
            }
        } else {
            linkUrl = UrlUtil.encodeAndReplace(linkUrl);
            linkUrl = UriHelper.getRedirectUri(URLEncoder.encode(linkUrl, "UTF-8"));
            response.sendRedirect(linkUrl);
            return;
        }
    }
}
