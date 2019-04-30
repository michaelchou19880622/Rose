package com.bcs.core.model;

import java.math.BigDecimal;

import com.bcs.core.json.AbstractBcsEntity;

public class RewardCardModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	private String cardId;
	private String MID;
	private Integer pointGetAmount;
	private String pointGetTime;
	private String pointType;
	
	public String getCardId() {
		return cardId;
	}
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	
	public String getMID() {
		return MID;
	}
	public void setMID(String mID) {
		MID = mID;
	}
	
	public Integer getPointGetAmount() {
		return pointGetAmount;
	}
	public void setPointGetAmount(Integer pointGetAmount) {
		this.pointGetAmount = pointGetAmount;
	}
	
	public String getPointGetTime() {
		return pointGetTime;
	}
	public void setPointGetTime(String pointGetTime) {
		this.pointGetTime = pointGetTime;
	}
	
	public String getPointType() {
		return pointType;
	}
	public void setPointType(String pointType) {
		this.pointType = pointType;
	}
	
}
