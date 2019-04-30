package com.bcs.core.web.ui.page.enums;


/**
 * Page Mapping
 * 
 * Page1 : User Not Binded Page
 * Page2 : User Do Binding Page
 * Page3 : User Binded Page
 * Page4 : User Get Card Page
 * Page5 : User Card Page
 */
public enum MobilePageEnum {
	// 404
	Page404("/Mobile/Default/Page404", "404首頁"),
	// Redirect
	PageRedirect("/Mobile/Default/PageRedirect", "Redirect首頁"),
	
	// 未升級首頁
//	UserNotBindedPage("/Mobile/Views/Page1-2", "未升級首頁"),
	
	// 服務條款頁面
//	UserReadTermsOfBusinessPage("/Mobile/Views/Page7", "服務條款頁面"),
	
	// 升級輸入頁面
//	UserDoBindingPage("/Mobile/Views/Page2-1", "升級輸入頁面"),
//	UserDoBindingFailPage("/Mobile/Views/Page6", "升級輸入失敗頁面"),
	
	// 升級成功頁面
//	UserBindedPage("/Mobile/Views/Page3-2", "升級成功頁面"), 
	
	// 取得宜家卡頁面
//	UserGetCardPage("/Mobile/Views/Page4-1", "取得宜家卡頁面"),
//	UserGetCardFailPage("/Mobile/Views/Page10", "取得宜家卡失敗頁面"),
	
	// 宜家卡頁面
//	UserCardPage("/Mobile/Views/Page5-1", "宜家卡頁面"),
	
	// 修改 喜好店頁面
//	UserChangeShopPage("/Mobile/Views/Page8", "修改喜好店頁面"),
//	UserChangeShopOKPage("/Mobile/Views/Page9", "修改喜好店成功頁面"),
	
	// 優惠券頁面 新
	UserCouponIndexPage("/Mobile/Coupon/index", "優惠劵列表頁面"),
	UserCouponContentPage("/Mobile/Coupon/coupon_content", "優惠劵頁面"),
	UserCouponSelectStorePage("/Mobile/Coupon/select_store", "優惠劵使用選擇"),
	UserCouponMsgPage("/Mobile/Coupon/coupon_alert1", "優惠券訊息頁面"),
	UserCouponFillOutInfoPage("/Mobile/Coupon/fill_out_info", "優惠券訊息頁面"),
	
	// 集點卡頁面 新
	UserRewardCardIndexPage("/Mobile/RewardCard/index", "集點卡列表頁面"),
	UserRewardCardContentPage("/Mobile/RewardCard/rewardCard_content", "集點卡頁面"),
	UserRewardCardMsgPage("/Mobile/RewardCard/rewardCard_alert1", "集點卡訊息頁面"),
	UserNoRewardCardMsgPage("/Mobile/RewardCard/msg", "查無集點卡訊息頁面"),
	
//	UserShareIndexPage("/Mobile/Share/ShareIndex", "分享頁面-初次"),
//	UserShareBindedPage("/Mobile/Share/ShareBinded", "分享頁面"),
//	UserShareContentPage("/Mobile/Share/ShareContent", "分享訊息頁面"),
//	UserShareNotMobilePage("/Mobile/Share/ShareNotMobile", "分享非手機頁面"),
//	UserShareRulePage("/Mobile/Share/ShareRule", "分享注意事項頁面"),

	UserTracingStartPage("/Mobile/Tracing/TracingStart", "追蹤起始頁面"),
	// 遊戲首頁
	TurntableIndexPage("/Mobile/Game/TurntableIndexPage2", "轉盤抽獎首頁"),
//	TurntableGetPrizePage("/Mobile/Game/TurntableGetPrizePage", "轉盤領獎首頁"),

//	TurntableIndexPage("/Mobile/Game/TurntableIndexPage", "轉盤抽獎首頁"),
	ScratchCardIndexPage("/Mobile/Game/ScratchCardIndexPage", "刮刮卡首頁"),
	AcceptPrizePage("/Mobile/Game/AcceptPrizePage", "領獎頁面"),
	LineSharePage("/Mobile/Game/LineSharePage", "已領獎頁面"),
	WinPrizePage("/Mobile/Game/WinPrizePage", "已領獎頁面"),
	PrizeAcceptedPage("/Mobile/Game/PrizeAcceptedPage", "已領獎頁面"),
//	AcceptPrizePage_Invoice("/Mobile/Game/AcceptPrizePage_Invoice", "領獎頁面"),
	
	DoMgmPage("/Mobile/Mgm/MgmPage","MGM頁面"),
    MgmTracingStartPage("/Mobile/Tracing/MgmTracingStart", "MGM追蹤起始頁面"),
    MgmRedirectPage("/Mobile/Mgm/MgmRedirectPage", "MGM跳轉頁面"),
	;

    private final String str;
    private final String title;
    
    MobilePageEnum(String str, String title) {
        this.str = str;
        this.title = title;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
	
	public String getTitle() {
		return title;
	}

	public String getName(){
		return this.name();
	}
}
