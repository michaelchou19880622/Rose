package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class LinkClickReportSearchModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	private String queryFlag;
	private Integer page;
	
	public String getQueryFlag() {
		return queryFlag;
	}
	public void setQueryFlag(String queryFlag) {
		this.queryFlag = queryFlag;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
}
