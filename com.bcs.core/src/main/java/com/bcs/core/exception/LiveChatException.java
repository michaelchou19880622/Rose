package com.bcs.core.exception;

public class LiveChatException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public LiveChatException() {
        super();
    }
	
	public LiveChatException(String message) {
        super(message);
    }
	
	public LiveChatException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public LiveChatException(Throwable cause) {
        super(cause);
    }
}
