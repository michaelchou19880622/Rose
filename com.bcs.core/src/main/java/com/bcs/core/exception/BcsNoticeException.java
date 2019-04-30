package com.bcs.core.exception;

public class BcsNoticeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String status;

	public BcsNoticeException(String message) {
        super(message);
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
