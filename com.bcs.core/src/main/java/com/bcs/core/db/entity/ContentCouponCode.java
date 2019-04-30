package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.bcs.core.json.CustomDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import scala.annotation.tailrec;

@Entity
@Table(name = "BCS_CONTENT_COUPON_CODE",
	indexes = {
		   @Index(name = "INDEX_0", columnList = "COUPON_CODE_ID"),
	       @Index(name = "INDEX_1", columnList = "COUPON_ID"),
	       @Index(name = "INDEX_2", columnList = "ACTION_USER_COUPON_ID")
	},
	uniqueConstraints={
	    @UniqueConstraint(columnNames = {"COUPON_ID", "COUPON_CODE"})
	}
)
public class ContentCouponCode extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	/* 已被領取的優惠券序號 */
	public static final String COUPON_CODE_IS_USED = "IS_USED";
	
	/* 尚未被領取的優惠券序號 */
	public static final String COUPON_CODE_IS_NOT_USE = "IS_NOT_USE";

	public ContentCouponCode(){
		
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "COUPON_CODE_ID")
	private Long couponCodeId;

	@Column(name = "COUPON_ID")
	private String couponId;

	@Column(name = "COUPON_CODE")
	private String couponCode;

	@Column(name = "ACTION_USER_COUPON_ID")
	private Long actionUserCouponId;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	public Long getCouponCodeId() {
		return couponCodeId;
	}

	public void setCouponCodeId(Long couponCodeId) {
		this.couponCodeId = couponCodeId;
	}

	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public Long getActionUserCouponId() {
		return actionUserCouponId;
	}

	public void setActionUserCouponId(Long actionUserCouponId) {
		this.actionUserCouponId = actionUserCouponId;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
	
}
