package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class SendMsgDetailModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String detailType;
	private String detailContent;
	private String eventType;

	public String getDetailType() {
		return detailType;
	}

	public void setDetailType(String detailType) {
		this.detailType = detailType;
	}

	public String getDetailContent() {
		return detailContent;
	}

	public void setDetailContent(String detailContent) {
		this.detailContent = detailContent;
	}

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
	
}
