package com.bcs.web.ui.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bcs.core.json.AbstractBcsEntity;

public class LinkClickReportModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String linkTitle;
	
	private String linkUrl;
	
	private String linkId;
	
	private String linkTime;
	
	private String linkFlag;
	
	private Set<String> flags = new HashSet<String>();
	
	private Long totalCount = 0L;
	
	private Long userCount = 0L;
	
	public String getLinkTitle() {
		return linkTitle;
	}
	public void setLinkTitle(String linkTitle) {
		this.linkTitle = linkTitle;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
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
	public String getLinkTime() {
		return linkTime;
	}
	public void setLinkTime(String linkTime) {
		this.linkTime = linkTime;
	}
	public String getLinkFlag() {
		return linkFlag;
	}
	public void setLinkFlag(String linkFlag) {
		this.linkFlag = linkFlag;
	}
	public Set<String> getFlags() {
		return flags;
	}
	public void addFlags(List<String> flags) {
		this.flags.addAll(flags);
	}
	public String getLinkId() {
		return linkId;
	}
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
}
