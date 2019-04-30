package com.bcs.core.model;

import java.math.BigDecimal;

import com.bcs.core.json.AbstractBcsEntity;

public class CouponModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	private String couponId;
	private String couponTitle;
	private String couponImageId;
	private String couponListImageId;
	private String couponDescription;
	private String couponUseDescription;
	private String couponRuleDescription;
	private BigDecimal probability;
	private String identityLetter;
	
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
	
	public String getCouponImageId() {
		return couponImageId;
	}
	public void setCouponImageId(String couponImageId) {
		this.couponImageId = couponImageId;
	}
	
	public String getCouponListImageId() {
		return couponListImageId;
	}
	public void setCouponListImageId(String couponListImageId) {
		this.couponListImageId = couponListImageId;
	}
	
	public String getCouponDescription() {
		return couponDescription;
	}
	public void setCouponDescription(String couponDescription) {
		this.couponDescription = couponDescription;
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
	
	public BigDecimal getProbability() {
		return probability;
	}
	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}
	
	public String getIdentityLetter() {
		return identityLetter;
	}
	public void setIdentityLetter(String identityLetter) {
		this.identityLetter = identityLetter;
	}
}
