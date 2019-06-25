package com.bcs.core.linepoint.api.model;

import java.util.Date;

import javax.persistence.Column;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;

public class LinePointResponseModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private String transcationId;
	private Long transcationTime;
	private String transcationType;	
	private Integer transactionAmount;
	private Integer balance;
	public String getTranscationId() {
		return transcationId;
	}
	public void setTranscationId(String transcationId) {
		this.transcationId = transcationId;
	}
	public Long getTranscationTime() {
		return transcationTime;
	}
	public void setTranscationTime(Long transcationTime) {
		this.transcationTime = transcationTime;
	}
	public String getTranscationType() {
		return transcationType;
	}
	public void setTranscationType(String transcationType) {
		this.transcationType = transcationType;
	}
	public Integer getTransactionAmount() {
		return transactionAmount;
	}
	public void setTransactionAmount(Integer transactionAmount) {
		this.transactionAmount = transactionAmount;
	}
	public Integer getBalance() {
		return balance;
	}
	public void setBalance(Integer balance) {
		this.balance = balance;
	}
	
}