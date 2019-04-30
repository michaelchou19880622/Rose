package com.bcs.core.record.akke.model;

public class RecordMsg {

	private Object msg;
	
	private int retry = 0;
	
	private boolean isSuccess = false;
	
	public RecordMsg(Object msg){
		this.setMsg(msg);
	}

	public Object getMsg() {
		return msg;
	}

	public void setMsg(Object msg) {
		this.msg = msg;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
}
