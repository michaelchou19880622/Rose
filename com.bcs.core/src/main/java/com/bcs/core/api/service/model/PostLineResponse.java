package com.bcs.core.api.service.model;

public class PostLineResponse {

	private int status;
	private String responseStr;
	
	public PostLineResponse(int status, String responseStr){
		this.setStatus(status);
		this.setResponseStr(responseStr);
	}

	public int getStatus() {
		return status;
	}

	private void setStatus(int status) {
		this.status = status;
	}

	public String getResponseStr() {
		return responseStr;
	}

	private void setResponseStr(String responseStr) {
		this.responseStr = responseStr;
	}
}
