package com.bcs.core.enums;

public enum EVENT_TARGET_ACTION_TYPE {
			
	EVENT_SHARE("EventShare"),
		ACTION_ShareWinning("ShareWinning"),

	EVENT_SEND_GROUP("SendGroup"),
		ACTION_UPLOAD_MID("UploadMid"),

	EVENT_SERIAL_SETTING("SerialSetting"),
		ACTION_UPLOAD_MID_SERIAL("UploadMidSerial"),
	;

    private final String str;
    
    EVENT_TARGET_ACTION_TYPE(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
