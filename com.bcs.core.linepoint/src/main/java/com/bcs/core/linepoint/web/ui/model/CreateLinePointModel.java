package com.bcs.core.linepoint.web.ui.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class CreateLinePointModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private List<CreateLinePointModel> richMenuImgUrls;
	private String richType;
	private String richMenuName;
	private String richMenuTitle;
	private String richMenuShowStatus;
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
	private String changeCondition;
	private String menuSize;
	private String richMenuStartUsingTime;
	private String richMenuEndUsingTime;
	private Long richMenuGroupId;
	
	public List<CreateLinePointModel> getRichMenuImgUrls() {
		return richMenuImgUrls;
	}
	public void setRichMsgImgUrls(List<CreateLinePointModel> richMenuImgUrls) {
		this.richMenuImgUrls = richMenuImgUrls;
	}
	
	public String getRichType() {
		return richType;
	}
	public void setRichType(String richType) {
		this.richType = richType;
	}
	
	public String getRichMenuName() {
		return richMenuName;
	}
	public void setRichMenuName(String richMenuName) {
		this.richMenuName = richMenuName;
	}
	
	public String getRichMenuTitle() {
		return richMenuTitle;
	}
	public void setRichMenuTitle(String richMenuTitle) {
		this.richMenuTitle = richMenuTitle;
	}
	
	public String getRichMenuShowStatus() {
		return richMenuShowStatus;
	}
	public void setRichMenuShowStatus(String richMenuShowStatus) {
		this.richMenuShowStatus = richMenuShowStatus;
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
	public String getChangeCondition() {
		return changeCondition;
	}
	public void setChangeCondition(String changeCondition) {
		this.changeCondition = changeCondition;
	}
	public String getMenuSize() {
		return menuSize;
	}
	public void setMenuSize(String menuSize) {
		this.menuSize = menuSize;
	}
	public String getRichMenuStartUsingTime() {
		return richMenuStartUsingTime;
	}
	public void setCouponStartUsingTime(String richMenuStartUsingTime) {
		this.richMenuStartUsingTime = richMenuStartUsingTime;
	}
	public String getRichMenuEndUsingTime() {
		return richMenuEndUsingTime;
	}
	public void setRichMenuEndUsingTime(String richMenuEndUsingTime) {
		this.richMenuEndUsingTime = richMenuEndUsingTime;
	}
	public Long getRichMenuGroupId() {
		return richMenuGroupId;
	}
	public void setRichMenuGroupId(Long richMenuGroupId) {
		this.richMenuGroupId = richMenuGroupId;
	}
}
