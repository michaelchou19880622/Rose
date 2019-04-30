package com.bcs.core.enums;

public enum LIVE_CHAT_WORDING {
	LIVE_CHAT_START("live.chat.start"),
	LIVE_CHAT_START_FAIL("live.chat.start.fail"),
	
	LIVE_CHAT_CLOSE("live.chat.close"),
	LIVE_CHAT_CLOSE_PASSIVE("live.chat.close.passive"),
	
	USER_IS_ALREADY_WAITING("user.is.already.waiting"),
	USER_IS_ALREADY_IN_PROGRESS("user.is.already.in.progess"),
	USER_HAS_ALREADY_CHOOSE_CATEGORY("user.has.already.choose.category"),
	USER_CANNOT_START("user.cannot.start"),
	
	NOT_IN_OFFICE_HOUR("not.in.office.hour"),
	
	SEND_MESSAGE_FAIL("send.message.fail"),
	
	DEFAULT_REPLY_MESSAGE("default.reply.message"),
	
	CHOOSE_CATAGORY("choose.category"),
	
	CUSTOMER_SERVICE_BUSY("customer.service.busy"),
	CUSTOMER_SERVICE_STILL_BUSY("customer.service.still.busy"),
	
	SWITCH_MESSAGE_NORMAL("switch.message.normal"),
	SWITCH_MESSAGE_KEYWORD("switch.message.keyword"),
	SWITCH_MESSAGE_MARKETING("switch.message.marketing"),
	WORDING_SPLIT_KEY("wording.split.key"),
	
	MESSAGE_BOARD("message.board"),
	LEAVE_MESSAGE_INTRO("leave.message.intro"),
	LEAVE_MESSAGE_START("leave.message.start"),
	LEAVE_MESSAGE_CONFIRM("leave.message.confirm"),
	LEAVE_MESSAGE_CHOOSE("leave.message.choose"),
	LEAVE_MESSAGE_COMPLETE("leave.message.complete"),
	LEAVE_MESSAGE_RESET("leave.message.reset"),
	
	WAITING_MESSAGE("waiting.message"),
	GIVEUP_MESSAGE("giveup.message"),
	
	SUCCESS_SWITCH("success.switch"),
	GIVEUP_SWITCH("giveup.switch"),
	
	SWITCH_MESSAGE_NORMAL_TITLE("switch.message.normal.title"),
	SWITCH_MESSAGE_KEYWORD_TITLE("switch.message.keyword.title"),
	
	NOT_IN_OFFICE_HOUR_TITLE("not.in.office.hour.title"),
	
	CUSTOMER_SERVICE_BUSY_TITLE("customer.service.busy.title"),
	CUSTOMER_SERVICE_STILL_BUSY_TITLE("customer.service.still.busy.title"),
	
    SWITCH_BUTTON("switch.button"),
    LEAVE_MESSAGE_BUTTON("leave.message.button"),
    KEEP_CHATTING_BUTTON("keep.chatting.button"),
    KEEP_WAITING_BUTTON("keep.wating.button"),
    GIVEUP_WAITING_BUTTON("giveup.wating.button"),
    CUSTOMER_SATISFACTION_DEGREE_BUTTON("customer.satisfaction.degree.button"),
    CONFIRM_LEAVE_MESSAGE_BUTTON("confirm.leave.message.button"),
    RESET_LEAVE_MESSAGE_BUTTON("reset.leave.message.button"),
    
    CATEGORY_NAME_ZH_TW("category.name.zh_tw"),
    CATEGORY_NAME_EN("category.name.en");
	
	private final String str;
    
	LIVE_CHAT_WORDING(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
