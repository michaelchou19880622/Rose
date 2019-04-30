package com.bcs.core.record.akke.model;

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.json.AbstractBcsEntity;

public class WebLoginClickLinkModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String code;
	private String mid;
	private String state;
	private ContentLink contentLink;
	private String sessionId;
	
	public WebLoginClickLinkModel(String code, String mid, String state, ContentLink contentLink, String sessionId){
		this.code= code;
		this.mid= mid;
		this.state= state;
		this.contentLink= contentLink;
		this.sessionId = sessionId;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public ContentLink getContentLink() {
		return contentLink;
	}
	public void setContentLink(ContentLink contentLink) {
		this.contentLink = contentLink;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	} 
	
}
