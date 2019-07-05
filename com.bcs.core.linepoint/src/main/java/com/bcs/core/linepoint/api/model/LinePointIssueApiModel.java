package com.bcs.core.linepoint.api.model;

import java.util.Date;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;

public class LinePointIssueApiModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private String department;
	private String serviceName;
	private String pccCode;
	private String campName;
	private String uid;
	private String custid;
	private String orderKey;
	private Integer amount;
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getPccCode() {
		return pccCode;
	}
	public void setPccCode(String pccCode) {
		this.pccCode = pccCode;
	}
	public String getCampName() {
		return campName;
	}
	public void setCampName(String campName) {
		this.campName = campName;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getCustid() {
		return custid;
	}
	public void setCustid(String custid) {
		this.custid = custid;
	}
	public String getOrderKey() {
		return orderKey;
	}
	public void setOrderKey(String orderKey) {
		this.orderKey = orderKey;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	

	
}