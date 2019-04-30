package com.bcs.web.ui.model;

import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class TracingLinkModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private List<SendMsgDetailModel> linkData;
	private String tracingId;

	public List<SendMsgDetailModel> getLinkData() {
		return linkData;
	}

	public void setLinkData(List<SendMsgDetailModel> linkData) {
		this.linkData = linkData;
	}

	public String getTracingId() {
		return tracingId;
	}

	public void setTracingId(String tracingId) {
		this.tracingId = tracingId;
	}
	
}
