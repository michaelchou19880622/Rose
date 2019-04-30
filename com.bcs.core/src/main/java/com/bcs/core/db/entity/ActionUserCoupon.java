package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "BCS_ACTION_USER_COUPON",
indexes = {
		   @Index(name = "INDEX_4", columnList = "ID"),
	       @Index(name = "INDEX_0", columnList = "MID"),
	       @Index(name = "INDEX_1", columnList = "COUPON_ID"),
	       @Index(name = "INDEX_2", columnList = "ACTION_TYPE"),
	       @Index(name = "INDEX_3", columnList = "WINNER_LIST_ID")
	})
public class ActionUserCoupon extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	public static final String ACTION_TYPE_USE = "USE";
	public static final String ACTION_TYPE_GET = "GET";

	public ActionUserCoupon(){
		
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "MID", columnDefinition = "nvarchar(50)")
	private String mid;

	@Column(name = "COUPON_ID")
	private String couponId;

	@Column(name = "ACTION_TYPE", columnDefinition = "nvarchar(50)")
	private String actionType;

	@Column(name = "ACTION_TIME")
	private Date actionTime;

	@Column(name = "COUPON_SINDEX")
	private Long couponSIndex;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "COUPON_START_USING_TIME")
	private Date couponStartUsingTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "COUPON_END_USING_TIME")
	private Date couponEndUsingTime;
	
	@Column(name = "COUPON_CODE_ID")
	private Long couponCodeId;
	
	@Column(name = "WINNER_LIST_ID", columnDefinition="nvarchar(50)")
	private String winnerListId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Date getActionTime() {
		return actionTime;
	}

	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}

	public Long getCouponSIndex() {
		return couponSIndex;
	}

	public void setCouponSIndex(Long couponSIndex) {
		this.couponSIndex = couponSIndex;
	}

	public Date getCouponStartUsingTime() {
		return couponStartUsingTime;
	}

	public void setCouponStartUsingTime(Date couponStartUsingTime) {
		this.couponStartUsingTime = couponStartUsingTime;
	}

	public Date getCouponEndUsingTime() {
		return couponEndUsingTime;
	}

	public void setCouponEndUsingTime(Date couponEndUsingTime) {
		this.couponEndUsingTime = couponEndUsingTime;
	}
	
	public Long getCouponCodeId() {
		return couponCodeId;
	}

	public void setCouponCodeId(Long couponCodeId) {
		this.couponCodeId = couponCodeId;
	}

	public String getWinnerListId() {
		return winnerListId;
	}

	public void setWinnerListId(String winnerListId) {
		this.winnerListId = winnerListId;
	}
	
}
