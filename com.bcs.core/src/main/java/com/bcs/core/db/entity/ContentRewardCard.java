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
@Table(name = "BCS_CONTENT_REWARD_CARD",
indexes = {
	       @Index(name = "INDEX_0", columnList = "STATUS"),
	})
public class ContentRewardCard extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	/**
	 * 僅限一次
	 */
	public static final String REWARD_CARD_USING_LIMIT_ONCE = "OstarNCE";
	
	/**
	 * 有效期間內不限次數
	 */
	public static final String REWARD_CARD_USING_LIMIT_UNLIMITED = "UNLIMITED";
	
	/**
	 * 一天一次
	 */
	public static final String REWARD_CARD_USING_LIMIT_ONCE_A_DAY = "ONCE_A_DAY";

	public static final String REWARD_CARD_STATUS_DELETE = "DELETE";
	public static final String REWARD_CARD_STATUS_ACTIVE = "ACTIVE";
	public static final String REWARD_CARD_STATUS_DISABLE = "DISABLE";

	public static final String REWARD_CARD_FLAG_PRIVATE = "PRIVATE";
	
	@Id
	@Column(name = "REWARDCARD_ID", columnDefinition = "nvarchar(50)")
	private String rewardCardId;

	@Column(name = "REWARDCARD_MAINTITLE", columnDefinition = "nvarchar(50)")
	private String rewardCardMainTitle;
	
	@Column(name = "REWARDCARD_SUBTITLE", columnDefinition = "nvarchar(50)")
	private String rewardCardSubTitle;
	
	@Column(name = "REWARDCARD_BACKGROUND", columnDefinition = "nvarchar(50)")
	private String rewardCardBackGround;
	
	@Column(name = "REWARDCARD_LIST_IMAGE_ID", columnDefinition = "nvarchar(500)")
	private String rewardCardListImageId;
	
	@Column(name = "REWARDCARD_REQUIRE_POINT")
	private Long requirePoint;
	
	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REWARDCARD_START_USING_TIME")
	private Date rewardCardStartUsingTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REWARDCARD_END_USING_TIME")
	private Date rewardCardEndUsingTime;
	
//	@Column(name = "REWARDCARD_EXPIRE_TYPE")
//	private String expireType;
//	
//	@Column(name = "REWARDCARD_EXPIRE_MONTH")
//	private Long expireMonth;
//	
//	@Column(name = "REWARDCARD_EXPIRE_NOTIFICATION")
//	private Long expireNotification;
	
	@Column(name = "REWARDCARD_BONUS_POINT")
	private Long bonusPoint;
	
	@Column(name = "REWARDCARD_LIMIT_GET_TIME")
	private Long limitGetTime;
	
	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REWARDCARD_START_GET_TIME")
	private Date rewardCardStartGetTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REWARDCARD_END_GET_TIME")
	private Date rewardCardEndGetTime;
	
	@Column(name = "REWARDCARD_GET_NUMBER")
	private int rewardCardGetNumber;

	@Column(name = "REWARDCARD_USE_DESCRIPTION", columnDefinition = "nvarchar(1000)")
	private String rewardCardUseDescription;
	
	@Column(name = "REWARDCARD_DESCRIPTION", columnDefinition = "nvarchar(1000)")
	private String rewardCardDescription;
	
	@Column(name = "STATUS", columnDefinition = "nvarchar(20)")
	private String status;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
	private String modifyUser;
	
	@Column(name = "REWARDCARD_LIMIT_GET_NUMBER")
	private Long limitGetNumber;
	
	@Column(name = "REWARDCARD_FLAG", columnDefinition="nvarchar(50)")
	private String rewardCardFlag;

	public Long getLimitGetNumber() {
        return limitGetNumber;
    }

    public void setLimitGetNumber(Long limitGetNumber) {
        this.limitGetNumber = limitGetNumber;
    }

    public String getRewardCardId() {
		return rewardCardId;
	}

	public void setRewardCardId(String rewardCardId) {
		this.rewardCardId = rewardCardId;
	}

	public String getRewardCardMainTitle() {
		return rewardCardMainTitle;
	}

	public void setRewardCardMainTitle(String rewardCardMainTitle) {
		this.rewardCardMainTitle = rewardCardMainTitle;
	}
	
	public String getRewardCardSubTitle() {
		return rewardCardSubTitle;
	}

	public void setRewardCardSubTitle(String rewardCardSubTitle) {
		this.rewardCardSubTitle = rewardCardSubTitle;
	}
	
	public String getRewardCardBackGround() {
		return rewardCardBackGround;
	}

	public void setRewardCardBackGround(String rewardCardBackGround) {
		this.rewardCardBackGround = rewardCardBackGround;
	}
	
	public String getRewardCardListImageId() {
		return rewardCardListImageId;
	}

	public void setRewardCardListImageId(String rewardCardListImageId) {
		this.rewardCardListImageId = rewardCardListImageId;
	}
	
	public Long getRequirePoint() {
		return requirePoint;
	}

	public void setRequirePoint(Long requirePoint) {
		this.requirePoint = requirePoint;
	}
	
	public Date getRewardCardStartUsingTime() {
		return rewardCardStartUsingTime;
	}

	public void setRewardCardStartUsingTime(Date rewardCardStartUsingTime) {
		this.rewardCardStartUsingTime = rewardCardStartUsingTime;
	}

	public Date getRewardCardEndUsingTime() {
		return rewardCardEndUsingTime;
	}

	public void setRewardCardEndUsingTime(Date rewardCardEndUsingTime) {
		this.rewardCardEndUsingTime = rewardCardEndUsingTime;
	}
	
//	public String getExpireType() {
//		return expireType;
//	}
//
//	public void setExpireType(String expireType) {
//		this.expireType = expireType;
//	}
//	
//	public Long getExpireMonth() {
//		return expireMonth;
//	}
//
//	public void setExpireMonth(Long expireMonth) {
//		this.expireMonth = expireMonth;
//	}
//	
//	public Long getExpireNotification() {
//		return expireNotification;
//	}
//
//	public void setExpireNotification(Long expireNotification) {
//		this.expireNotification = expireNotification;
//	}
	
	public Long getBonusPoint() {
		return bonusPoint;
	}
	
	public void setBonusPoint(Long bonusPoint) {
		this.bonusPoint = bonusPoint;
	}
	
	public Long getLimitGetTime() {
		return limitGetTime;
	}
	
	public void setLimitGetTime(Long limitGetTime) {
		this.limitGetTime = limitGetTime;
	}
	
	public Date getRewardCardStartGetTime() {
		return rewardCardStartGetTime;
	}

	public void setRewardCardStartGetTime(Date rewardCardStartGetTime) {
		this.rewardCardStartGetTime = rewardCardStartGetTime;
	}

	public Date getRewardCardEndGetTime() {
		return rewardCardEndGetTime;
	}

	public void setRewardCardEndGetTime(Date rewardCardEndGetTime) {
		this.rewardCardEndGetTime = rewardCardEndGetTime;
	}
	
	public int getRewardCardGetNumber() {
		return rewardCardGetNumber;
	}

	public void setRewardCardGetNumber(int rewardCardGetNumber) {
		this.rewardCardGetNumber = rewardCardGetNumber;
	}
	
	public String getRewardCardUseDescription() {
		return rewardCardUseDescription;
	}

	public void setRewardCardUseDescription(String rewardCardUseDescription) {
		this.rewardCardUseDescription = rewardCardUseDescription;
	}
	
	public String getRewardCardDescription() {
		return rewardCardDescription;
	}

	public void setRewardCardDescription(String rewardCardDescription) {
		this.rewardCardDescription = rewardCardDescription;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public String getRewardCardFlag() {
		return rewardCardFlag;
	}

	public void setRewardCardFlag(String rewardCardFlag) {
		this.rewardCardFlag = rewardCardFlag;
	}
	
}
