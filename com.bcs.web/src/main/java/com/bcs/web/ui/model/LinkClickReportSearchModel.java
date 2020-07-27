package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class LinkClickReportSearchModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	private String queryFlag;
	private Integer page;
	private Integer pageSize;
	private String startDate;
	private String endDate;
	private String dataStartDate;
	private String dataEndDate;
	
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
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getDataStartDate() {
		return dataStartDate;
	}
	public void setDataStartDate(String dataStartDate) {
		this.dataStartDate = dataStartDate;
	}
	public String getDataEndDate() {
		return dataEndDate;
	}
	public void setDataEndDate(String dataEndDate) {
		this.dataEndDate = dataEndDate;
	}
}
