package com.bcs.core.utils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.Model;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bcs.core.api.service.LineFriendShipStatusService;
import com.bcs.core.api.service.LineProfileService;
import com.bcs.core.api.service.LineWebLoginApiService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LineLoginUtil {
	/** Logger */
	private static Logger logger = Logger.getLogger(LineLoginUtil.class);

    public static void addLineoauthLinkInModel(Model model, String RedirectUrl, String tracingIdStr) throws Exception {
    	addLineoauthLinkInModel(CONFIG_STR.Default.toString(), model, RedirectUrl, tracingIdStr);
    }
    
    public static void addLineoauthLinkInModel(String target, Model model, String RedirectUrl, String tracingIdStr) throws Exception {
        String ChannelID = CoreConfigReader.getString(target, CONFIG_STR.ChannelID.toString(), true);

        String lineoauthLink = "";
        lineoauthLink = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_V2_1);
        lineoauthLink = lineoauthLink.replace("{ChannelID}", ChannelID);
        lineoauthLink = lineoauthLink.replace("{RedirectUrl}",
                URLEncoder.encode(RedirectUrl, "UTF-8"));
        lineoauthLink = lineoauthLink.replace("{TracingId}", tracingIdStr);
        
        model.addAttribute("lineoauthLink", lineoauthLink);
    }
    public static Map<String, String> callRetrievingAPI(String code, String redirectUrl, String state) throws Exception {
    	return callRetrievingAPI(CONFIG_STR.Default.toString(), code, redirectUrl, state);
    }
    
    public static Map<String, String> callRetrievingAPI(String target, String code, String redirectUrl, String state) throws Exception {

        String ChannelID = CoreConfigReader.getString(target, CONFIG_STR.ChannelID.toString(), true);
        String ChannelSecret = CoreConfigReader.getString(target, CONFIG_STR.ChannelSecret.toString(), true);

        Map<String, String> resultMap = new HashMap<String, String>();
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        
        ObjectNode result = context.getBean(LineWebLoginApiService.class).callRetrievingAPI(ChannelID, ChannelSecret, code,
                redirectUrl);
        
        if (result != null) {
            
            String UID = null;
            
            if (result.get("id_token") != null) {
                String id_token = result.get("id_token").asText();
                
                if (StringUtils.isNotBlank(id_token)) {
                    UID = jwtGetUid(id_token, ChannelSecret, state);
                    if(StringUtils.isNotBlank(UID)) {
                        resultMap.put("UID", UID);
                    }
                }
            }
            
            if(result.get("access_token") != null) {
                String access_token = result.get("access_token").asText();
                
                if (StringUtils.isNotBlank(access_token)) {
                    
                    if(StringUtils.isBlank(UID)) {
                        ObjectNode getProfile = context.getBean(LineProfileService.class).callGetProfileAPI(access_token);

                        if (getProfile != null && getProfile.get("userId") != null
                                && StringUtils.isNotBlank(getProfile.get("userId").asText())) {
                            resultMap.put("UID", getProfile.get("userId").asText());
                            UID = getProfile.get("userId").asText();
                        } 
                        
                        UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineLoginUtil, LOG_TARGET_ACTION_TYPE.ACTION_CallGetProfileAPI_API, UID, getProfile, ":callRetrievingAPI:" +state);
                    }
                    
                    // Get FriendShip Status
                    ObjectNode getFriendShipStatus = context.getBean(LineFriendShipStatusService.class).getFriendShipStatusService(access_token);
                    if (getFriendShipStatus != null && getFriendShipStatus.get("friendFlag") != null
                            && StringUtils.isNotBlank(getFriendShipStatus.get("friendFlag").asText())) {
                        resultMap.put("friendFlag", getFriendShipStatus.get("friendFlag").asText());
                    }else{
                        resultMap.put("friendFlag", "0");
                    }
                    
                    UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineLoginUtil, LOG_TARGET_ACTION_TYPE.ACTION_CallRetrievingAPI, UID, code + "--" + redirectUrl, ":callRetrievingAPI:" +state);
                    UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineLoginUtil, LOG_TARGET_ACTION_TYPE.ACTION_CallRetrievingAPI_API, UID, result, ":callRetrievingAPI:" +state); 
                    UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineLoginUtil, LOG_TARGET_ACTION_TYPE.ACTION_GetFriendShipStatus_API, UID, getFriendShipStatus, ":callRetrievingAPI:" +state);
                }
            }
        }

        return resultMap;
    }
    
    public static String jwtGetUid(String id_token, String ChannelSecret, String state) {

    	String UID = null;
        if (StringUtils.isNotBlank(id_token)) {
        	try {
        		DecodedJWT jwt = JWT.decode(id_token);
//        	    Algorithm algorithm = Algorithm.HMAC256(ChannelSecret);
//        	    JWTVerifier verifier = JWT.require(algorithm)
//        	        .withIssuer("https://access.line.me")
//        	        .build(); //Reusable verifier instance
//        	    DecodedJWT jwt = verifier.verify(id_token);
        	    if(jwt != null) {
        	    	String sub = jwt.getSubject();
	            	if(StringUtils.isNotBlank(sub)) {
	            		UID = sub;
	            	}
					UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineLoginUtil, LOG_TARGET_ACTION_TYPE.ACTION_Call_JWT, sub, jwt, ":callRetrievingAPI:" +state);
        	    }
            	
        	} catch (Exception exception){
        	    //Invalid signature/claims
    			logger.error(ErrorRecord.recordError(exception));
        	}
        }
		UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineLoginUtil, LOG_TARGET_ACTION_TYPE.ACTION_Call_JWT, UID, id_token, ":callRetrievingAPI:" +state);
        
        return UID;
    }

    public static void getFrienShipStatus(String code, String sessionMID, HttpServletRequest request,String state, String redirectUrl) throws Exception {
    	getFrienShipStatus(CONFIG_STR.Default.toString(), code, sessionMID, request, state, redirectUrl);
    }
    
    public static void getFrienShipStatus(String target, String code, String sessionMID, HttpServletRequest request,String state, String redirectUrl) throws Exception {
    	logger.info("getFriendShipStatus : " );
        String ChannelID = CoreConfigReader.getString(target, CONFIG_STR.ChannelID.toString(),true);
        String ChannelSecret = CoreConfigReader.getString(target, CONFIG_STR.ChannelSecret.toString(), true);
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        
        ObjectNode result = context.getBean(LineWebLoginApiService.class).callRetrievingAPI(ChannelID, ChannelSecret, code,
                redirectUrl);

        if (result != null && result.get("access_token") != null) {
            String access_token = result.get("access_token").asText();
            if (StringUtils.isNotBlank(access_token)) {

                ObjectNode getFriendShipStatus = context.getBean(LineFriendShipStatusService.class).getFriendShipStatusService(access_token);
                logger.info("getFriendShipStatus : " + getFriendShipStatus);
                
            }
        }
    }
    
    public static String getLineoauthLink(String RedirectUrl, String tracingIdStr) throws Exception {
        
        return getLineoauthLink(CONFIG_STR.Default.toString(), RedirectUrl, tracingIdStr);
    }
    
    public static String getLineoauthLink(String target,String RedirectUrl, String tracingIdStr) throws Exception {
        String ChannelID = CoreConfigReader.getString(target,CONFIG_STR.ChannelID.toString(), true);
        
        String lineoauthLink = "";
        lineoauthLink = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_V2_1);
        lineoauthLink = lineoauthLink.replace("{ChannelID}", ChannelID);
        lineoauthLink = lineoauthLink.replace("{RedirectUrl}",URLEncoder.encode(RedirectUrl, "UTF-8"));
        lineoauthLink = lineoauthLink.replace("{TracingId}", tracingIdStr);
        
        return lineoauthLink;
    }
}
