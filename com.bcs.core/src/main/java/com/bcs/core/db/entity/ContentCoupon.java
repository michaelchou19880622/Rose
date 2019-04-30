package com.bcs.core.db.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "BCS_CONTENT_COUPON",
indexes = {
	       @Index(name = "INDEX_0", columnList = "STATUS"),
	})
public class ContentCoupon extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	/**
	 * 僅限一次
	 */
	public static final String COUPON_USING_LIMIT_ONCE = "ONCE";
	
	/**
	 * 有效期間內不限次數
	 */
	public static final String COUPON_USING_LIMIT_UNLIMITED = "UNLIMITED";
	
	/**
	 * 一天一次
	 */
	public static final String COUPON_USING_LIMIT_ONCE_A_DAY = "ONCE_A_DAY";

	public static final String COUPON_STATUS_DELETE = "DELETE";
	public static final String COUPON_STATUS_ACTIVE = "ACTIVE";
	public static final String COUPON_STATUS_DISABLE = "DISABLE";

	public static final String COUPON_FLAG_PRIVATE = "PRIVATE";
	
	/**
	 * 使用優惠券的活動類型(刮刮樂,集點卡)
	 */
	public static final String EVENT_REFERENCE_REWARD_CARD = "REWARD_CARD";
	public static final String EVENT_REFERENCE_SCRATCH_CARD = "SCRATCH_CARD";
	public static final String EVENT_REFERENCE_GAME = "GAME";
	
	public static final boolean  IS_COUPON_CODE_TRUE = true;
	public static final boolean  IS_COUPON_CODE_FALSE = false;
	
	public static final boolean  IS_COUPON_FILLIN_TRUE = true;
	public static final boolean  IS_COUPON_FILLIN_FALSE = false;
	
	@Id
//	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "COUPON_ID")
	private String couponId;

	@Column(name = "COUPON_TITLE", columnDefinition = "nvarchar(50)")
	private String couponTitle;

	@Column(name = "COUPON_DESCRIPTION", columnDefinition = "nvarchar(1000)")
	private String couponDescription;

	@Column(name = "COUPON_USE_DESC", columnDefinition = "nvarchar(1000)")
	private String couponUseDescription;

	@Column(name = "COUPON_RULE_DESC", columnDefinition = "nvarchar(1000)")
	private String couponRuleDescription;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "COUPON_START_GET_TIME")
	private Date couponStartGetTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "COUPON_END_GET_TIME")
	private Date couponEndGetTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "COUPON_START_USING_TIME")
	private Date couponStartUsingTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "COUPON_END_USING_TIME")
	private Date couponEndUsingTime;

	@Column(name = "COUPON_USING_LIMIT", columnDefinition = "nvarchar(50)")
	private String couponUsingLimit;

	@Column(name = "COUPON_SERIAL_NUMBER", columnDefinition = "nvarchar(50)")
	private String couponSerialNumber;

	@Column(name = "COUPON_GET_NUMBER")
	private int couponGetNumber;

	@Column(name = "COUPON_GET_LIMIT_NUMBER")
	private Integer couponGetLimitNumber;
	
	@Column(name = "COUPON_USING_NUMBER")
	private int couponUsingNumber;

	@Column(name = "COUPON_TYPE", columnDefinition = "nvarchar(50)")
	private String couponType;

	@Column(name = "COUPON_LIST_IMAGE_ID", columnDefinition = "nvarchar(500)")
	private String couponListImageId;

	@Column(name = "COUPON_IMAGE_ID", columnDefinition = "nvarchar(500)")
	private String couponImageId;
	
	@Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
	private String modifyUser;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "COUPON_FLAG", columnDefinition="nvarchar(50)")
	private String couponFlag;

	@Column(name = "EVENT_REFERENCE", columnDefinition="nvarchar(50)")
	private String eventReference;
	
	/* @Column(name = "EVENT_REFERENCE_ID")
	private Long eventReferenceId; */
	
	@Column(name = "EVENT_REFERENCE_ID")
	private String eventReferenceId;
	
	@Column(name = "REQUIRE_POINT")
	private Long requirePoint;
	
	@Column(name = "PROBABILITY")
	private BigDecimal probability;
	
	@Column(name = "IS_FILL_IN")
	private Boolean isFillIn;

	@Column(name = "IS_COUPON_CODE")
	private Boolean isCouponCode;
	
	@Column(name = "IDENTITY_LETTER", columnDefinition = "nvarchar(5)")
	private String identityLetter;
	
	@Column(name = "COUPON_REMARK",columnDefinition="text")
	private String couponRemark;
	
	@Column(name = "COUPON_GROUP_ID", columnDefinition = "nvarchar(50)")
	private String couponGroupId;

	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}

	public String getCouponTitle() {
		return couponTitle;
	}

	public void setCouponTitle(String couponTitle) {
		this.couponTitle = couponTitle;
	}

	public String getCouponDescription() {
		return couponDescription;
	}

	public void setCouponDescription(String couponDescription) {
		this.couponDescription = couponDescription;
	}

	public Date getCouponStartGetTime() {
		return couponStartGetTime;
	}

	public void setCouponStartGetTime(Date couponStartGetTime) {
		this.couponStartGetTime = couponStartGetTime;
	}

	public Date getCouponEndGetTime() {
		return couponEndGetTime;
	}

	public void setCouponEndGetTime(Date couponEndGetTime) {
		this.couponEndGetTime = couponEndGetTime;
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

	public String getCouponUsingLimit() {
		return couponUsingLimit;
	}

	public void setCouponUsingLimit(String couponUsingLimit) {
		this.couponUsingLimit = couponUsingLimit;
	}

	public String getCouponSerialNumber() {
		return couponSerialNumber;
	}

	public void setCouponSerialNumber(String couponSerialNumber) {
		this.couponSerialNumber = couponSerialNumber;
	}

	public int getCouponGetNumber() {
		return couponGetNumber;
	}

	public void setCouponGetNumber(int couponGetNumber) {
		this.couponGetNumber = couponGetNumber;
	}

	public int getCouponUsingNumber() {
		return couponUsingNumber;
	}

	public void setCouponUsingNumber(int couponUsingNumber) {
		this.couponUsingNumber = couponUsingNumber;
	}

	public String getCouponType() {
		return couponType;
	}

	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}

	public String getCouponImageId() {
		return couponImageId;
	}

	public void setCouponImageId(String couponImageId) {
		this.couponImageId = couponImageId;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Integer getCouponGetLimitNumber() {
		return couponGetLimitNumber;
	}

	public void setCouponGetLimitNumber(Integer couponGetLimitNumber) {
		this.couponGetLimitNumber = couponGetLimitNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCouponUseDescription() {
		return couponUseDescription;
	}

	public void setCouponUseDescription(String couponUseDescription) {
		this.couponUseDescription = couponUseDescription;
	}

	public String getCouponRuleDescription() {
		return couponRuleDescription;
	}

	public void setCouponRuleDescription(String couponRuleDescription) {
		this.couponRuleDescription = couponRuleDescription;
	}

	public String getCouponListImageId() {
		return couponListImageId;
	}

	public void setCouponListImageId(String couponListImageId) {
		this.couponListImageId = couponListImageId;
	}

	public String getCouponFlag() {
		return couponFlag;
	}

	public void setCouponFlag(String couponFlag) {
		this.couponFlag = couponFlag;
	}

	public String getEventReference() {
		return eventReference;
	}

	public void setEventReference(String eventReference) {
		this.eventReference = eventReference;
	}
	
	public String getEventReferenceId() {
		return eventReferenceId;
	}

	public void setEventReferenceId(String eventReferenceId) {
		this.eventReferenceId = eventReferenceId;
	}
	
	public Long getRequirePoint() {
		return requirePoint;
	}

	public void setRequirePoint(Long requirePoint) {
		this.requirePoint = requirePoint;
	}
	
	public BigDecimal getProbability() {
		return probability;
	}

	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	public Boolean getIsFillIn() {
		return isFillIn;
	}

	public void setIsFillIn(Boolean isFillIn) {
		this.isFillIn = isFillIn;
	}

	public Boolean getIsCouponCode() {
		return isCouponCode;
	}

	public void setIsCouponCode(Boolean isCouponCode) {
		this.isCouponCode = isCouponCode;
	}
	
	public String getIdentityLetter() {
		return identityLetter;
	}

	public void setIdentityLetter(String identityLetter) {
		this.identityLetter = identityLetter;
	}

	public String getCouponRemark() {
		return couponRemark;
	}

	public void setCouponRemark(String couponRemark) {
		this.couponRemark = couponRemark;
	}

	public String getCouponGroupId() {
		return couponGroupId;
	}

	public void setCouponGroupId(String couponGroupId) {
		this.couponGroupId = couponGroupId;
	}
	
}
