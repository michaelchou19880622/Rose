package com.bcs.core.enums;

public enum DB_CONFIG {

	PRE_env("env"),
	
	COLL_setting("setting"),

	COLL_LineMsgReceiving("LineMsgReceiving"),
	COLL_LineMsgValidate("LineMsgValidate"),
	COLL_LineSend("LineSend"),
	COLL_LineSendResult("LineSendResult"),
	COLL_LineMsgGet("LineMsgGet"),
	;

    private final String str;
    
    DB_CONFIG(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
