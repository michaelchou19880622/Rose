package com.bcs.core.enums;

public enum API_TYPE {
	BC("BC"),
	BOT("BOT"),
	;

    private final String str;
    
    API_TYPE(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
