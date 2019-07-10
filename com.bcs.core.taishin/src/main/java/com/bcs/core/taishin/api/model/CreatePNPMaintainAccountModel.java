package com.bcs.core.taishin.api.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import java.util.List;

public class CreatePNPMaintainAccountModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String accountType;
	private String account; //帳號
	private String accountAttribute; //帳號屬性
	private String accountClass;  //帳號類別
	private String sourceSystem;  //前方來源系統
	private String departmentId;  //單位代號
	private String employeeId; //員工編號
	private String divisionName;	// 處
	private String departmentName;  // 部	
	private String groupName; // 組
	private String pccCode;
	private Boolean status; //帳號狀態
	private List<CreatePNPMaintainAccountActionModel> actions; 

	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String accout) {
		this.account = accout;
	}
	public String getSourceSystem() {
		return sourceSystem;
	}
	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}
	public String getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getPccCode() {
		return pccCode;
	}
	public void setPccCode(String pccCode) {
		this.pccCode = pccCode;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getAccountAttribute() {
		return accountAttribute;
	}
	public void setAccountAttribute(String accountAttribute) {
		this.accountAttribute = accountAttribute;
	}
	public String getAccountClass() {
		return accountClass;
	}
	public void setAccountClass(String accountClass) {
		this.accountClass = accountClass;
	}
	public String getDivisionName() {
		return divisionName;
	}
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public List<CreatePNPMaintainAccountActionModel> getActions() {
		return actions;
	}
	public void setActions(List<CreatePNPMaintainAccountActionModel> actions) {
		this.actions = actions;
	}
}
