package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL")
public class ActionUserRewardCardPointDetail extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	//點數為手動還自動
	public static final String POINT_TYPE_MANUAL = "MANUAL";
	public static final String POINT_TYPE_AUTOMATIC = "AUTOMATIC";
	public static final String POINT_TYPE_SYSTEM = "SYSTEM";
	
	public ActionUserRewardCardPointDetail(){
		
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "USER_REWARD_CARD_ID")
	private Long userRewardCardId;

	@Column(name = "POINT_GET_TIME")
	private Date pointGetTime;
	
	@Column(name = "POINT_GET_AMOUNT")
	private int pointGetAmount;
	
	@Column(name = "REFERENCE_ID", columnDefinition = "nvarchar(50)")
	private String referenceId;
	
	@Column(name = "POINT_TYPE", columnDefinition="nvarchar(50)")
	private String pointType;
	
	@Column(name = "REWARDCARD_POINT_ID", columnDefinition="nvarchar(50)")
	private String rewardCardPointId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserRewardCardId() {
		return userRewardCardId;
	}

	public void setUserRewardCardId(Long userRewardCardId) {
		this.userRewardCardId = userRewardCardId;
	}

	public Date getPointGetTime() {
		return pointGetTime;
	}

	public void setPointGetTime(Date pointGetTime) {
		this.pointGetTime = pointGetTime;
	}
	
	public int getPointGetAmount() {
		return pointGetAmount;
	}

	public void setPointGetAmount(int getAmount) {
		this.pointGetAmount = getAmount;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	
	public String getRewardCardPointId() {
		return rewardCardPointId;
	}

	public void setRewardCardPointId(String rewardCardPointId) {
		this.rewardCardPointId = rewardCardPointId;
	}
	
	public String getPointType() {
		return pointType;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}
}
