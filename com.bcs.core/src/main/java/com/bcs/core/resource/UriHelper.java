package com.bcs.core.resource;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bcs.core.enums.BCS_PAGE_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.UrlUtil;

public class UriHelper {
	/** Logger */
	private static Logger logger = Logger.getLogger(UriHelper.class);
	
	public static final String TO_TYPE_MID = "MID";
	public static final String TO_TYPE_PHONE = "PHONE";
	
	public static String bcsMPage = CoreConfigReader.getString(CONFIG_STR.M_PAGE);

	static String baseUrl_Https = CoreConfigReader.getString(CONFIG_STR.BaseUrlHTTPS);
//	static String baseUrl_Http = CoreConfigReader.getString(CONFIG_STR.BaseUrlHTTP);
	
	static String pageBcs = CoreConfigReader.getString(CONFIG_STR.PageBCS);
	static String resourceBCS = CoreConfigReader.getString(CONFIG_STR.ResourceBCS);
	
	static String pageMobile = CoreConfigReader.getString(CONFIG_STR.PageMobile);
	static String resourceMobile = CoreConfigReader.getString(CONFIG_STR.ResourceMobile);
	
	static String resourceApi = CoreConfigReader.getString("rest.api.path.resource");
	static String resourceLink = CoreConfigReader.getString("rest.api.path.link.resource");

	static String tracingUrl = CoreConfigReader.getString("rest.api.path.tracing.link");
	static String oauthUrl = CoreConfigReader.getString("rest.api.path.oauth");
	
	static String previewImage = CoreConfigReader.getString("rest.api.path.bcs.preview.image");
	static String bcsLogo = CoreConfigReader.getString("rest.api.path.bcs.logo");

	static String staticSrcUrl = CoreConfigReader.getString("bcs.base.url.static.src");

	public static String getResourceUri(String type, String id){
		return getResourceUri(type, id, true);
	}
	public static String getResourceUri(String type, String id, boolean isSSL){

		if(isSSL){
			return baseUrl_Https + pageBcs + resourceApi + type + "/" + id;
		}
		else{
			return baseUrl_Https + pageBcs + resourceApi + type + "/" + id;
		}
	}
	
	public static String getStaticResourceUri(String type, String id){
		
		boolean useStaticSrc = CoreConfigReader.getBoolean(CONFIG_STR.SRC_USE_STATIC, true);

		if(useStaticSrc){
		    String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + type + System.getProperty("file.separator") + id;
			File genfile = new File(filePath);
			if(!genfile.exists()){
				return null;
			}
		    
			return staticSrcUrl + type + "/" + id;
		}
		else{
			return null;
		}
	}
	
	public static String getLinkUri(String id){

		return baseUrl_Https + pageBcs + resourceLink + id;
	}
	
	public static String getLinkUri(String id, String MID){

		String result = baseUrl_Https + pageBcs + resourceLink + id + "?MID={from}" + "&time={time}&hash={hash}";
		result = UrlUtil.encodeAndHash(result, MID, null);
		return result;
	}
	
	public static String getLinkUriSerialId(String id, String MID, String serialId){

		String result =  baseUrl_Https + pageBcs + resourceLink + id + "?MID={from}" + "&serialId={code}&time={time}&hash={hash}";
		result = UrlUtil.encodeAndHash(result, MID, serialId);
		return result;
	}
	
	public static String getLinkUri(String id, String MID, String replace){

		String result =  baseUrl_Https + pageBcs + resourceLink + id + "?MID={from}" + "&replace={code}&time={time}&hash={hash}";
		result = UrlUtil.encodeAndHash(result, MID, replace);
		return result;
	}
	
	public static String getLinkUriWithType(String id, String MID, String type){

		String result =  baseUrl_Https + pageBcs + resourceLink + id + "?MID={from}" + "&type={code}&time={time}&hash={hash}";
		result = UrlUtil.encodeAndHash(result, MID, type);
		return result;
	}
	
	public static String getLinkUriCode(String id, String code){

		return baseUrl_Https + pageBcs + resourceLink + id + "?code=" + code;
	}
	
	public static String getLinkUriCode(String id, String code, String event){

		if(StringUtils.isNotBlank(code) && StringUtils.isNotBlank(event)){
			return baseUrl_Https + pageBcs + resourceLink + id + "?code=" + code + "&event=" + event;
		}
		else if(StringUtils.isNotBlank(code)){
			return baseUrl_Https + pageBcs + resourceLink + id + "?code=" + code;
		}
		else if(StringUtils.isNotBlank(event)){
			return baseUrl_Https + pageBcs + resourceLink + id + "?event=" + event;
		}
		else{
			return baseUrl_Https + pageBcs + resourceLink + id;
		}
	}
	
	public static String getTracingUrlPre(){

		return baseUrl_Https + tracingUrl ;
	}
	
	public static String getTracingUrl(Long tracingId){

		return baseUrl_Https + tracingUrl + tracingId;
	}
	
	public static String getOauthUrl(){

		return baseUrl_Https + oauthUrl;
	}
	
	public static String getResourcePreviewUri(String type, String preview, String id){
		
		return baseUrl_Https + pageBcs + resourceApi + type + "/" + preview + "/" + id;
	}
	
	public static String getResourcePreviewImageUri(){
		
		return baseUrl_Https + resourceBCS + previewImage;
	}
	
	public static String getResourceBcsLogoUri(){
		
		return baseUrl_Https + resourceMobile + bcsLogo;
	}
	
	public static String getIndexUri(){

		return baseUrl_Https + pageMobile + "index";
	}
	
	public static String getRedirectUri(String redirect){

		return baseUrl_Https + pageMobile + "redirect?redirect=" + redirect;
	}
	
//	public static String getIndexUri(String event){
//
//		return baseUrl_Https + pageMobile + "index?event=" + event;
//	}

	public static String getGoIndexUri(){

		return baseUrl_Https + pageMobile + "goIndex";
	}
	
	public static String getGoIndexUri(String MID){

		String result =  baseUrl_Https + pageMobile + "goIndex" + "?MID={from}" + "&time={time}&hash={hash}";
		result = UrlUtil.encodeAndHash(result, MID, null);
		return result;
	}
//	
//	public static String getChangeShopUri(){
//
//		return baseUrl_Https + pageMobile + "changeShop";
//	}
//	
//	public static String getChangeShopOKUri(){
//		return baseUrl_Https + pageMobile + "changeShopOK";
//	}
//	
	public static String getReadTermsOfBusinessUri(){

		return baseUrl_Https + pageMobile + "userBindingPage";
	}
//	
	public static String getUserCouponListPageUri(){

		return baseUrl_Https + pageMobile + "userCouponIndexPage";
	}
	
	public static String getUserRewardCardListPageUri(String MID){
		logger.info("@@@@@@@@@@@getUserRewardCardListPageUri："+baseUrl_Https + pageMobile + "userRewardCardIndexPage");
		return baseUrl_Https + pageMobile + "userRewardCardIndexPage"+"?MID="+MID;
	}
	
//	public static String getShareContentPagePageUri(){
//
//		return baseUrl_Https + pageMobile + "shareContentPage";
//	}
	
	public static String getUserCouponPageUri(String referenceId){

		return baseUrl_Https + pageMobile + "userCouponContentPage" + "?referenceId=" + referenceId;
	}
	
	public static String getUserRewardCardPageUri(String referenceId){

		return baseUrl_Https + pageMobile + "userRewardCardContentPage" + "?referenceId=" + referenceId;
	}
	
	public static String getAddPointUserRewardCardPageUri(String referenceId){

		return baseUrl_Https + pageMobile + "createActionUserRewardCardForUsePage" + "?referenceId=" + referenceId;
	}
	
	public static String getUserTurntablePageUri(String referenceId, boolean refresh){

		if(refresh){
			return baseUrl_Https + pageMobile + "turntableIndexPage" + "?gameId=" + referenceId + "&refresh=" + refresh;
		}
		else{
			return baseUrl_Https + pageMobile + "turntableIndexPage" + "?gameId=" + referenceId;
		}
	}
	
	public static String getUserScratchCardPageUri(String MID,String referenceId, boolean refresh){

		if(refresh){
			return baseUrl_Https + pageMobile + "scratchCardIndexPage" + "?gameId=" + referenceId +"&UID="+MID+ "&refresh=" + refresh;
		}
		else{
			return baseUrl_Https + pageMobile + "scratchCardIndexPage" + "?gameId=" + referenceId+"&UID="+MID;
		}
	}
//	
//	public static String getCardUri(){
//
//		return baseUrl_Https + pageMobile + "userGetCardPage";
//	}
//
//	public static String getUserCardPageUri(){
//		return baseUrl_Https + pageMobile + "userCardPage";
//	}
//	
//	public static String getUserGetCardFailPageUri(){
//		return baseUrl_Https + pageMobile + "userGetCardFailPage";
//	}
//	
	public static String getIndexToPageUri(String MID, String toPage){

		String result =  baseUrl_Https + pageMobile + "goIndex" + "?MID={from}" +"&toPage={code}&time={time}&hash={hash}";
		result = UrlUtil.encodeAndHash(result, MID, toPage);
		return result;
	}
	
	public static String getIndexToPageUri(String MID, String toPage, String referenceId){

		if(StringUtils.isNotBlank(MID)){
			String result =  baseUrl_Https + pageMobile + "goIndex" + "?MID={from}" +"&toPage=" + toPage + "&referenceId={code}&time={time}&hash={hash}" ;
			result = UrlUtil.encodeAndHash(result, MID, referenceId);
			return result;
		}
		else{
			return baseUrl_Https + pageMobile + "goIndex" + "?toPage=" + toPage +"&referenceId=" + referenceId ;
		}
	}
	
	public static String getRewardCardGetPointUri(String rewardCardPointId) {
	    return baseUrl_Https + pageMobile + "RewardCard/getPoint/" + rewardCardPointId;
	}
	
	public static String getRewardCardValidateUri() {
	    return baseUrl_Https + pageMobile + "RewardCard/validate";
	}
	
	public static String getGeneratorQRCodeUri(String rewardCardPointId){
		 return pageBcs + "edit/generatorQRCode/" + rewardCardPointId;
	}
	
	public static String goScratchCardUri() {
	    return baseUrl_Https + pageMobile + "Game/goScratchCardByQRcode";
	}
	
	public static String getScratchCardValidateUri() {
	    return baseUrl_Https + pageMobile + "Game/ScratchCard/validate";
	}
	
	public static String getVIPNightAuth(){
        return baseUrl_Https + "campaign/VIPNight/auth";
    }
	
	public static String getMgmClickOauth(){
        return baseUrl_Https + "c/" + "validate";
    }
    public static String getMgmClickTracingUrl(){
        return baseUrl_Https + "c/";
    }
    public static String getMgmOauth(){
        return baseUrl_Https + "c/m/" + "validate";
    }
    public static String getMgmTracingUrl(){
        return baseUrl_Https + "c/m/";
    }
    public static String getMgmPage(){
        return baseUrl_Https + pageMobile + "mgmPage";
    }
    public static String getGoMgmPage(String campaignId){
        return baseUrl_Https + pageMobile + "goMgmPage?campaignId=" + campaignId;
    }
    public static String getMgmRedirectPage(String originalRedirectUrl, String originalMsg) {
        StringBuffer sb = new StringBuffer(baseUrl_Https + pageMobile + "mgmRedirectPage?");
        
        try {
            if(StringUtils.isNotBlank(originalRedirectUrl)) {
                sb.append("replaceLink="+ URLEncoder.encode(originalRedirectUrl, "UTF-8").replace("+", "%20") +"&");
            }
            
            if(StringUtils.isNotBlank(originalMsg)) {
                sb.append("msg="+ URLEncoder.encode(originalMsg, "UTF-8").replace("+", "%20"));
            }
            
        }catch(Exception e) {}
        
        return sb.toString();
    }
    
    /**
     * Link Page Pattern Create
     * @param id
     * @return
     */
    public static String getMgmPagePattern(String campaignId){

        return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.MGM_CAMPAIGN_PAGE + ":" + campaignId;
    }
//	
//	public static String getUserDoBindingFailPageUri(){
//		return baseUrl_Https + pageMobile + "userDoBindingFailPage";
//	}
//	
//	/**
//	 * User Page Pattern Create
//	 * @return
//	 */
//	public static String getUserPagePattern(){
//
//		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_USER_PAGE;
//	}
//	
//	/**
//	 * Change Shop Page Pattern Create
//	 * @return
//	 */
//	public static String getChangeShopPagePattern(){
//
//		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_CHANGE_SHOP_PAGE;
//	}
//	
	/**
	 * Read Terms Of Business Page Pattern Create
	 * @return
	 */
	public static String getReadTermsOfBusinessPagePattern(){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_READ_TERMS_PAGE;
	}
//	
//	/**
//	 * Get Card Page Pattern Create
//	 * @return
//	 */
//	public static String getCardPagePattern(){
//
//		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_GET_CARD_PAGE;
//	}
	
	/**
	 * Get Coupon List Page Pattern Create
	 * @return
	 */
	public static String getCouponListPagePattern(){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_COUPON_LIST_PAGE;
	}
	
	public static String getRewardCardListPagePattern(){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE;
	}
	
//	/**
//	 * Get Share Content Page Pattern Create
//	 * @return
//	 */
//	public static String getShareContentPagePattern(){
//
//		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_SHARE_CONTENT_PAGE;
//	}
	
	/**
	 * Coupon Page Pattern Create
	 * @param id
	 * @return
	 */
	public static String getCouponPattern(String id){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_COUPON_PAGE + ":" + id;
	}
	
	public static String getRewardCardPattern(String id){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_REWARD_CARD_PAGE + ":" + id;
	}

	public static String getRewardCardAddPointPattern(String id){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_REWARD_CARD_ADD_POINT_PAGE + ":" + id;
	}
	
	/**
	 * TurnTable Page Pattern Create
	 * @param id
	 * @return
	 */
	public static String getTurntablePattern(String id){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE + ":" + id;
	}
	
	/**
	 * Scratch Page Pattern Create
	 * @param id
	 * @return
	 */
	public static String getScratchPattern(String id){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_SCRACTH_PAGE + ":" + id;
	}
	
	/**
	 * Link Page Pattern Create
	 * @param id
	 * @return
	 */
	public static String getLinkPattern(String id){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_LINK_PAGE + ":" + id;
	}

	public static String getSerialIdLinkPattern(String id, String serialId){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_LINK_PAGE + ":" + id + ";" + serialId;
	}
	
	/**
	 * Link Page Pattern Create
	 * @param id
	 * @param replace
	 * @return
	 */
	public static String getReplaceLinkPattern(String id, String replace){

		return BCS_PAGE_TYPE.PATTERN_START  + ":"+ BCS_PAGE_TYPE.TYPE_LINK_PAGE + ":" + id + ":" + replace;
	}
	
	public static boolean checkIsBcsPage(String patternUri){

		logger.debug("checkIsBcsPage:" + patternUri);

		// Pattern BcsPage:Type:resourceId
		if(patternUri.startsWith(BCS_PAGE_TYPE.PATTERN_START.toString())){
			return true;
		}
		
		return false;
	}
	
	public static boolean checkWithMidReplace(String patternUri){
		if(patternUri.indexOf("{from}") > 0){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Pattern BcsPage:Type:resourceId
	 * Type : UserPage
	 * Type : ShopChangePage
	 * Type : CouponPage
	 * Type : LinkPage
	 * 
	 * @param uri
	 * @param MID
	 * @return
	 */
	public static String parseBcsPage(String patternUri, String MID){

		logger.debug("parseBcsPage:" + patternUri + "-MID:" + MID);
		if(StringUtils.isNotBlank(MID)){
			try{
					
				// Pattern BcsPage:Type:resourceId
				if(patternUri.startsWith(BCS_PAGE_TYPE.PATTERN_START.toString())){
					String[] splits = patternUri.split(":");
					
					// User Page
//					if(BCS_PAGE_TYPE.TYPE_USER_PAGE.toString().equals(splits[1])){
//						return UriHelper.getGoIndexUri(MID);
//					}
//					// Change Shop Page
//					if(BCS_PAGE_TYPE.TYPE_CHANGE_SHOP_PAGE.toString().equals(splits[1])){
//						return UriHelper.getIndexToPageUri(MID, BCS_PAGE_TYPE.TYPE_CHANGE_SHOP_PAGE.toString());
//					}
					// Read Terms Of Business Page
					if(BCS_PAGE_TYPE.TYPE_READ_TERMS_PAGE.toString().equals(splits[1])){
						return UriHelper.getIndexToPageUri(MID, BCS_PAGE_TYPE.TYPE_READ_TERMS_PAGE.toString());
					}
//					// Get Card Page
//					if(BCS_PAGE_TYPE.TYPE_GET_CARD_PAGE.toString().equals(splits[1])){
//						return UriHelper.getIndexToPageUri(MID, BCS_PAGE_TYPE.TYPE_GET_CARD_PAGE.toString());
//					}
					// Coupon List Page
					else if(BCS_PAGE_TYPE.TYPE_COUPON_LIST_PAGE.toString().equals(splits[1])){
						return UriHelper.getIndexToPageUri(MID, BCS_PAGE_TYPE.TYPE_COUPON_LIST_PAGE.toString());
					}
					// RewardCard List Page
					else if(BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE.toString().equals(splits[1])){
						return UriHelper.getIndexToPageUri(MID, BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE.toString());
					}
					// Coupon Page
					if(BCS_PAGE_TYPE.TYPE_COUPON_PAGE.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:CouponPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.TYPE_COUPON_PAGE.toString());
						}
					}
					//RewardCard Page
					if(BCS_PAGE_TYPE.TYPE_REWARD_CARD_PAGE.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:CouponPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.TYPE_REWARD_CARD_PAGE.toString());
						}
					}
					if(BCS_PAGE_TYPE.TYPE_REWARD_CARD_ADD_POINT_PAGE.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:CouponPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.TYPE_REWARD_CARD_ADD_POINT_PAGE.toString());
						}
					}
					// Turntable Page
					if(BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:CouponPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE.toString());
						}
					}
					// Turntable Page
					if(BCS_PAGE_TYPE.TYPE_SCRACTH_PAGE.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:CouponPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.TYPE_SCRACTH_PAGE.toString());
						}
					}
					// Turntable Page
					if(BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE_REFRESH.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:CouponPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE_REFRESH.toString());
						}
					}
//					// Share Content Page
//					if(BCS_PAGE_TYPE.TYPE_SHARE_CONTENT_PAGE.toString().equals(splits[1])){
//						return UriHelper.getIndexToPageUri(MID, BCS_PAGE_TYPE.TYPE_SHARE_CONTENT_PAGE.toString());
//					}
					// Link Page
					if(BCS_PAGE_TYPE.TYPE_LINK_PAGE.toString().equals(splits[1])){
						String resourceId = splits[2];
						// BcsPage:LinkPage:resourceId
						if(StringUtils.isNotBlank(resourceId)){
							if(splits.length >3){
								String replace = splits[3];
								if(StringUtils.isNotBlank(replace)){
									return UriHelper.getLinkUri(resourceId, MID, replace);
								}
							}
							
							String[] splitSerialId = resourceId.split(";");
							if(splitSerialId.length == 2){
								return UriHelper.getLinkUriSerialId(splitSerialId[0], MID, splitSerialId[1]);
							}
							else{
								return UriHelper.getLinkUri(resourceId, MID);
							}
						}
					}
					
					if(BCS_PAGE_TYPE.MGM_CAMPAIGN_PAGE.toString().equals(splits[1])){
                        String resourceId = splits[2];
                        // BcsPage:MgmPage:resourceId
                        if(StringUtils.isNotBlank(resourceId)){
                            return UriHelper.getLinkUriWithType(resourceId, MID, BCS_PAGE_TYPE.MGM_CAMPAIGN_PAGE.toString());
                        }
					}
					
					return UriHelper.getGoIndexUri(MID);
				}
			}
			catch(Exception e){
				logger.error(ErrorRecord.recordError(e));
				return UriHelper.getGoIndexUri(MID);
			}
		}
		
		return patternUri;
	}
}
