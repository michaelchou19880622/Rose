package com.bcs.core.enums;

public enum LINE_HEADER {

	HEADER_ChannelSignature("X-LINE-ChannelSignature"),
	HEADER_BOT_ChannelSignature("X-Line-Signature"),
	
	HEADER_BOT_ServiceCode("X-Line-ServiceCode"),
	HEADER_ChannelToken("X-Line-ChannelToken"),
	
	HEADER_ChannelID("X-Line-ChannelID"),
	HEADER_ChannelSecret("X-Line-ChannelSecret"),
	HEADER_MID("X-Line-Trusted-User-With-ACL"),

	HEADER_BCS_ChannelSignature("X-BCS-Signature"),
	;

    private final String str;
    
    LINE_HEADER(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
