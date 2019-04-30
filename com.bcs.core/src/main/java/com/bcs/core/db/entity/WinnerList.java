package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_WINNER_LIST",

	indexes = {
		   @Index(name = "INDEX_0", columnList = "WINNER_LIST_ID"),
	       @Index(name = "INDEX_1", columnList = "COUPON_ID")
	},
	uniqueConstraints={
	    @UniqueConstraint(columnNames = {"COUPON_ID", "UID"})
	})
public class WinnerList extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "WINNER_LIST_ID", columnDefinition="nvarchar(50)")
	private String winnerListId;
	
	@Column(name = "COUPON_ID")
	private String couponId;
	
	@Column(name = "USER_NAME", columnDefinition="nvarchar(50)")
	private String userName;
	
	@Column(name = "USER_IDCARDNUMBER", columnDefinition="nvarchar(50)")
	private String userIdCardNumber;
	
	@Column(name = "USER_PHONENUMBER", columnDefinition="nvarchar(50)")
	private String userPhoneNumber;
	
	@Column(name = "USER_EMAIL", columnDefinition="nvarchar(50)")
	private String userEMail;
	
	@Column(name = "USER_ADDRESS", columnDefinition="nvarchar(50)")
	private String userAddress;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "UID")
	private String uid;
	
	@Column(name = "ACTION_USER_COUPON_ID")
	private Long actionUserCouponId;

	public String getWinnerListId() {
		return winnerListId;
	}

	public void setWinnerListId(String winnerListId) {
		this.winnerListId = winnerListId;
	}
	
	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
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
		return uid;
	}

	public void setUID(String uid) {
		this.uid = uid;
	}

	public Long getActionUserCouponId() {
		return actionUserCouponId;
	}

	public void setActionUserCouponId(Long actionUserCouponId) {
		this.actionUserCouponId = actionUserCouponId;
	}
}