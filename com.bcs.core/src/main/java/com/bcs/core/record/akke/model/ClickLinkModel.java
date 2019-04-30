package com.bcs.core.record.akke.model;

import java.util.Date;

import com.bcs.core.json.AbstractBcsEntity;

public class ClickLinkModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String linkId;
	private Date clickTime;
	
	public ClickLinkModel(String linkId, Date clickTime){
		this.linkId = linkId;
		this.setClickTime(clickTime);
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public Date getClickTime() {
		return clickTime;
	}

	public void setClickTime(Date clickTime) {
		this.clickTime = clickTime;
	}

}
