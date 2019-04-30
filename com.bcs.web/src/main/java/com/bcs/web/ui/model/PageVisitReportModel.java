package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class PageVisitReportModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String pageTitle;
	
	private String pageUrl;
	
	private Long totalCount = 0L;
	
	private Long userCount = 0L;

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public Long getUserCount() {
		return userCount;
	}

	public void setUserCount(Long userCount) {
		this.userCount = userCount;
	}
	
}
