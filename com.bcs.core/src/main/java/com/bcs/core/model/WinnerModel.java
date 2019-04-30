package com.bcs.core.model;

import java.util.Date;

import com.bcs.core.json.AbstractBcsEntity;

public class WinnerModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private String winnerListId;
	private String userName;
	private String userIdCardNumber;
	private String userPhoneNumber;	
	private String userEMail;
	private String userAddress;
	private Date modifyTime;
	private String UID;
	private String couponPrizeName;
	
	public String getWinnerListId() {
		return winnerListId;
	}
	public void setWinnerListId(String winnerListId) {
		this.winnerListId = winnerListId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserIdCardNumber() {
		return userIdCardNumber;
	}
	
	public void setUserIdCardNumber(String userIdCardNumber) {
		this.userIdCardNumber = userIdCardNumber;
	}
	public String getUserPhoneNumber() {
		return userPhoneNumber;
	}
	public void setUserPhoneNumber(String userPhoneNumber) {
		this.userPhoneNumber = userPhoneNumber;
	}
	
	public String getUserEMail() {
		return userEMail;
	}
	public void setUserEMail(String userEMail) {
		this.userEMail = userEMail;
	}
	
	public String getUserAddress() {
		return userAddress;
	}
	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public String getUID() {
		return UID;
	}
	public void setUID(String UID) {
		this.UID = UID;
	}
	
	public String getCouponPrizeName() {
		return couponPrizeName;
	}
	public void setCouponPrizeName(String couponPrizeName) {
		this.couponPrizeName = couponPrizeName;
	}
}
