package com.bcs.core.enums;

public enum BCS_PAGE_TYPE {

	PATTERN_START("BcsPage"),

	TYPE_LINK_PAGE("LinkPage"),
	
//	TYPE_USER_PAGE("UserPage"),
//	TYPE_CHANGE_SHOP_PAGE("ChangeShopPage"),
	TYPE_READ_TERMS_PAGE("ReadTermsOfBusinessPage"),
//	TYPE_GET_CARD_PAGE("GetCardPage"),

	TYPE_COUPON_LIST_PAGE("CouponListPage"),
	TYPE_COUPON_PAGE("CouponPage"),
	
	TYPE_REWARD_CARD_LIST_PAGE("RewardCardListPage"),
	TYPE_REWARD_CARD_PAGE("RewardCardPage"),
	TYPE_REWARD_CARD_ADD_POINT_PAGE("RewardCardAddPointPage"),
	
//	TYPE_SHARE_CONTENT_PAGE("ShareContentPage"),
	TYPE_TURNTABLE_PAGE("TurntablePage"),
	TYPE_TURNTABLE_PAGE_REFRESH("TurntablePageRefersh"),
	
	TYPE_SCRACTH_PAGE("ScratchCardPage"),
	
	MGM_CAMPAIGN_PAGE("MgmPage"),
	;

    private final String str;
    
    BCS_PAGE_TYPE(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
