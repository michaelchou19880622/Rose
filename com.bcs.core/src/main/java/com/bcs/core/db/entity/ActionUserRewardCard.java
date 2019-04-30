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
@Table(name = "BCS_ACTION_USER_REWARD_CARD",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MID"),
	       @Index(name = "INDEX_1", columnList = "REWARD_CARD_ID"),
	       @Index(name = "INDEX_2", columnList = "ACTION_TYPE"),
	})
public class ActionUserRewardCard extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	public static final String ACTION_TYPE_USE = "USE";
	public static final String ACTION_TYPE_GET = "GET";

	public ActionUserRewardCard(){
		
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "MID", columnDefinition = "nvarchar(50)")
	private String mid;

	@Column(name = "REWARD_CARD_ID")
	private String rewardCardId;

	@Column(name = "ACTION_TYPE", columnDefinition = "nvarchar(50)")
	private String actionType;

	@Column(name = "ACTION_TIME")
	private Date actionTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REWARD_CARD_START_USING_TIME")
	private Date rewardCardStartUsingTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REWARD_CARD_END_USING_TIME")
	private Date rewardCardEndUsingTime;
	
	@Column(name = "NEXT_GET_POINT_TIME")
	private Date nextGetPointTime;

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

	public String getRewardCardId() {
		return rewardCardId;
	}

	public void setRewardCardId(String rewardCardId) {
		this.rewardCardId = rewardCardId;
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
	
	public Date getNextGetPointTime() {
		return nextGetPointTime;
	}

	public void setNextGetPointTime(Date nextGetPointTime) {
		this.nextGetPointTime = nextGetPointTime;
	}
	
}
