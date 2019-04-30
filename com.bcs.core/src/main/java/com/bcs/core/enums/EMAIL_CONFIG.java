package com.bcs.core.enums;

public enum EMAIL_CONFIG {

	HOST("mail.smtp.host"),
	
	AUTH("mail.smtp.auth"),

	STARTTLS_ENABLE("mail.smtp.starttls.enable"),
	PORT("mail.smtp.port"),
	USERNAME("mail.username"),
	PASSWORD("mail.password"),
	FROM("mail.from"),
	TO_ADMIN("mail.to.admin"),
	DEBUG("mail.debug"),
	;

    private final String str;
    
    EMAIL_CONFIG(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}
}
