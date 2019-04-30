package com.bcs.core.model;

import java.util.Date;

import com.bcs.core.json.AbstractBcsEntity;

public class WinnedCouponModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	private Long actionUserCouponId;
	private String userMID;
	private String couponTitle;
	private boolean isFillIn;
	private WinnerModel winnerDetail;
	private Date actionTime;
	private String couponCode;
	
	public Long getActionUserCouponId() {
		return actionUserCouponId;
	}
	public void setActionUserCouponId(Long actionUserCouponId) {
		this.actionUserCouponId = actionUserCouponId;
	}
		
	public String getUserMID() {
		return userMID;
	}
	public void setUserMID(String userMID) {
		this.userMID = userMID;
	}
	
	public String getCouponTitle() {
		return couponTitle;
	}
	public void setCouponTitle(String couponTitle) {
		this.couponTitle = couponTitle;
	}
	
	public boolean getIsFillIn() {
		return isFillIn;
	}
	public void setIsFillIn(boolean isFillIn) {
		this.isFillIn = isFillIn;
	}
	
	public WinnerModel getWinnerDetail() {
		return winnerDetail;
	}
	public void setWinnerDetail(WinnerModel winnerDetail) {
		this.winnerDetail = winnerDetail;
	}
	
	public Date getActionTime() {
		return actionTime;
	}
	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
}
