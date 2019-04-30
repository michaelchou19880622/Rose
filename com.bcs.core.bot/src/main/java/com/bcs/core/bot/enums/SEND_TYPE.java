package com.bcs.core.bot.enums;

public enum SEND_TYPE {

	REPLY_MSG("REPLY_MSG"),
	PUSH_MSG("PUSH_MSG"),
	;

    private final String str;
    
    SEND_TYPE(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}

}
