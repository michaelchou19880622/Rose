package com.bcs.core.taishin.circle.PNP.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_EMPLOYEE_RECORD")
public class EmployeeRecord extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "EMPLOYEE_ID", columnDefinition="nvarchar(100)")  //員工編號
	private String employeeId;
	
	@Column(name = "DEPARTMENT_ID", columnDefinition="nvarchar(100)")  //單位代號
	private String departmentId;

	@Column(name = "DIVISION_NAME", columnDefinition="nvarchar(100)")	// 處
	private String divisionName;
	
	@Column(name = "DEPARTMENT_NAME", columnDefinition="nvarchar(100)")  // 部
	private String departmentName;
	
	@Column(name = "GROUP_NAME", columnDefinition="nvarchar(100)")		// 組
	private String groupName;
	
	@Column(name = "PCC_CODE", columnDefinition="nvarchar(100)")
	private String pccCode;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public Long getId() {
		return id;
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
}
