package com.bcs.web.ui.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class RichMsgModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private List<RichMsgModel> richMsgImgUrls;
	private String richType;
	private String richTitle;
	private String richImageId;
	private String richDetailLetter;
	private int startPointX;
	private int startPointY;
	private int endPointX;
	private int endPointY;
	private String linkUrl;
	private String linkTitle;
	private List<String> linkTagList = new ArrayList<>();
	private String actionType;
	
	public List<RichMsgModel> getRichMsgImgUrls() {
		return richMsgImgUrls;
	}
	public void setRichMsgImgUrls(List<RichMsgModel> richMsgImgUrls) {
		this.richMsgImgUrls = richMsgImgUrls;
	}
	
	public String getRichType() {
		return richType;
	}
	public void setRichType(String richType) {
		this.richType = richType;
	}
	
	public String getRichTitle() {
		return richTitle;
	}
	public void setRichTitle(String richTitle) {
		this.richTitle = richTitle;
	}
	
	public String getRichImageId() {
		return richImageId;
	}
	public void setRichImageId(String richImageId) {
		this.richImageId = richImageId;
	}
	
	public String getRichDetailLetter() {
		return richDetailLetter;
	}
	public void setRichDetailLetter(String richDetailLetter) {
		this.richDetailLetter = richDetailLetter;
	}
	
	public int getStartPointX() {
		return startPointX;
	}
	public void setStartPointX(int startPointX) {
		this.startPointX = startPointX;
	}
	
	public int getStartPointY() {
		return startPointY;
	}
	public void setStartPointY(int startPointY) {
		this.startPointY = startPointY;
	}
	
	public int getEndPointX() {
		return endPointX;
	}
	public void setEndPointX(int endPointX) {
		this.endPointX = endPointX;
	}
	
	public int getEndPointY() {
		return endPointY;
	}
	public void setEndPointY(int endPointY) {
		this.endPointY = endPointY;
	}
	
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	
	public String getLinkTitle() {
		return linkTitle;
	}
	public void setLinkTitle(String linkTitle) {
		this.linkTitle = linkTitle;
	}
	public List<String> getLinkTagList() {
		return linkTagList;
	}
	public void setLinkTagList(List<String> linkTagList) {
		this.linkTagList = linkTagList;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
}
