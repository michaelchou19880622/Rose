package com.bcs.core.enums;

public enum LOG_TARGET_ACTION_TYPE {

	/**
	 * TARGET_LineUser
	 */
	TARGET_LineUser("LineUser"),
	
			ACTION_ChangeShop("ChangeShop"),
			ACTION_GetCard("GetCard"),
			ACTION_Unbind("Unbind"),
			ACTION_Binded2Block("Binded2Block"),
			ACTION_Block("Block"),
			ACTION_Binded("Binded"),
			ACTION_GetCardFail("GetCardFail"),
			ACTION_BindingFail("BindingFail"),
			ACTION_ChangeMid("ChangeMid"),
			ACTION_ChangeData("ChangeData"),
//			ACTION_UpdateFactContactLineStore("UpdateFactContactLineStore"),

	/**
	 * TARGET_MobilePage
	 */
	TARGET_MobilePage("MobilePage"),
	
			ACTION_VisitPage("VisitPage"),

	/**
	 * TARGET_ContentLink
	 */
	TARGET_ContentLink("ContentLink"),

			ACTION_ClickLink("ClickLink"),
			ACTION_ClickLinkWebLogin("ClickLinkWebLogin"),
			ACTION_ClickLinkWebLogin_API("ClickLinkWebLogin_API"),
	
	/**
	 * TARGET_InteractiveMsg
	 */
	TARGET_InteractiveMsg("InteractiveMsg"),
	
			ACTION_SendMatchMessage("SendMatchMessage"),

	/**
	 * TARGET_API
	 */
	TARGET_API("API"),
			
			ACTION_SMARTROBOT_API("SMARTROBOT_API"),
			ACTION_SMARTROBOT_API_Error("SMARTROBOT_Error"),
			
			ACTION_SMARTROBOT_BOT_API("SMARTROBOT_BOT_API"),
			ACTION_SMARTROBOT_BOT_API_Error("SMARTROBOT_BOT_Error"),
			
			ACTION_RICHART_API("RICHART_API"),
			ACTION_SendToRichartApiStatus("SendToRichartApiStatus"),

	/**
	 * TARGET_COUPON
	 */
	TARGET_COUPON("CouponDo"),

			ACTION_CouponList("CouponList"),
			ACTION_CouponGet("CouponGet"),
			ACTION_CouponUse("CouponUse"),
			ACTION_CouponCheckData("CouponCheckData"),
			
	/**
	 * TARGET_ReceivingMsgHandler
	 */
	TARGET_ReceivingMsgHandler("ReceivingMsgHandler"),
	
			ACTION_HandleMsgReceive("HandleMsgReceive"),
			ACTION_HandleMsgReceiveAll("HandleMsgReceiveAll"),
			
	/**
	 * TARGET_LineApi
	 */
	TARGET_LineApi("LineApi"),
	
			ACTION_SendToLineApiStatus("SendToLineApiStatus"),
			ACTION_GetFromLineApiStatus("GetFromLineApiStatus"),
			ACTION_GetFromLineApi("GetFromLineApi"),
			ACTION_GetFromLineApi_Error("GetFromLineApi_Error"),
			ACTION_SendToLineApi("SendToLineApi"),
			ACTION_SendToLineApi_Error("SendToLineApi_Error"),
			ACTION_ValidateLoginApi("ValidateLoginApi"),
			ACTION_CancelLogin("CancelLogin"),
			ACTION_ValidateLoginApi_Error("ValidateLoginApi_Error"),
			ACTION_VerifyApi("VerifyApi"),
			ACTION_VerifyApi_Error("VerifyApi_Error"),
			ACTION_RefreshingApi("RefreshingApi"),
			ACTION_RefreshingApi_Error("RefreshingApi_Error"),
			ACTION_ProfileApi("ProfileApi"),
			ACTION_ProfileApi_Error("ProfileApi_Error"),
			ACTION_GetConvertingAPI("GetConvertingAPI"),
			ACTION_GetConvertingAPI_Error("GetConvertingAPI_Error"),
			ACTION_PostConvertingAPI("PostConvertingAPI"),
			ACTION_PostConvertingAPI_Error("PostConvertingAPI_Error"),
			
			ACTION_SwitcherSwitchApi("ActionSwitcherSwitchApi"),
			ACTION_SwitcherSwitchApi_Error("ActionSwitcherSwitchApiError"),
			
			ACTION_FriendshipApi("FriendshipApi"),
            ACTION_FriendshipApi_Error("FriendshipApi_Error"),
            
            ACTION_SendPnpToLineApiStatus("SendPnpToLineApiStatus"),
            ACTION_SendPnpToLineApi("SendPnpToLineApi"),
            ACTION_SendPnpToLineApi_Error("SendPnpToLineApi_Error"),
	/**
	 * TARGET_LineBotApi
	 */
	TARGET_LineBotApi("LineBotApi"),
			ACTION_Receive("Receive"),
			
	TARGET_EventRecord("EventRecord"),

	/**
	 *  * TARGET_LineBCApiCluster
	 */
	TARGET_LineBCApiCluster("LineBCApiCluster"),
		ACTION_Send("Send"),
		ACTION_Send_Error("Send_Error"),
		
	/**
	 * TARGET_BcsApi
	 */
	TARGET_BcsApi("LineBcsApi"),
		ACTION_BcsApi_UpdateStatus("BcsApi_UpdateStatus"),
		ACTION_BcsApi_SendMessage("BcsApi_SendMessage"),
		
	TARGET_GameDo("GameDo"),
		ACTION_GameDo_GetPrize("GameDo_GetPrize"),
		ACTION_GameDo_AcceptedPrize("GameDo_AcceptedPrize"),
		ACTION_ShareTrigger("ShareTrigger"),
		
	TARGET_RichartApi("RichartApi"),
		ACTION_RichartApi_AdUserSync("RichartApi_AdUserSync"),
		ACTION_RichartApi_START_CHAT("RichartApi_StartChat"),
		ACTION_RichartApi_ADD_MESSAGE("RichartApi_AddMessage"),
		ACTION_RichartApi_CLOSE_CHAT("RichartApi_CloseChat"),
		
	TARGET_RichartAdApi("RichartAdApi"),
		ACTION_RichartAdApi_AdUserSync("RichartAdApi_AdUserSync"),
	TARGET_RichartLogApi("RichartLogApi"),
		ACTION_RichartLogAPi("RichartLogApi"),
		ACTION_RichartLogAPi_Error("RichartLogApi_Error"),
	
	TARGET_REWARD_CARD("RewardCardDo"),

			ACTION_RewardCardList("RewardCardList"),
			ACTION_RewardCardGet("RewardCardGet"),
			ACTION_RewardCardUse("RewardCardUse"),
			ACTION_RewardCardCheckData("RewardCardCheckData"),
			
	TARGET_LineLoginUtil("LineLoginUtil"),
	        ACTION_CallRetrievingAPI("CallRetrievingAPI"),
	        ACTION_CallRetrievingAPI_API("CallRetrievingAPI_API"),
	        ACTION_CallGetProfileAPI_API("CallGetProfileAPI_API"),
	        ACTION_Call_JWT("Call_JWT"),
	        ACTION_GetFriendShipStatus_API("GetFriendShipStatus_API"),
	        
    TARGET_ShareUserRecord("ShareUserRecord"),
	        ACTION_DoMgmSharing("DoMgmSharing"),
	;

    private final String str;
    
    LOG_TARGET_ACTION_TYPE(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
