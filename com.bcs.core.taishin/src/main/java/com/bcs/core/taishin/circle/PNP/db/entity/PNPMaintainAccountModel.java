package com.bcs.core.taishin.circle.PNP.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_PNP_MAINTAIN_ACCOUNT")
public class PNPMaintainAccountModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String NORMAL_ACCOUNT = "Normal";
	public static final String UNICA_ACCOUNT = "Unica";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ACCOUNT_TYPE", columnDefinition="nvarchar(50)") // Normal/Unica
	private String accountType;
	
	@Column(name = "ACCOUNT", columnDefinition="nvarchar(50)")
	private String account;

	@Column(name = "ACCOUNT_ATTRIBUTE", columnDefinition="nvarchar(50)")
	private String accountAttribute;
	
	@Column(name = "ACCOUNT_CLASS", columnDefinition="nvarchar(50)")
	private String accountClass;
	
	@Column(name = "SOURCE_SYSTEM", columnDefinition="nvarchar(50)")
	private String sourceSystem;

	@Column(name = "DEPARTMENT_ID", columnDefinition="nvarchar(50)")
	private String departmentId;
	
	@Column(name = "EMPLOYEE_ID", columnDefinition="nvarchar(50)")
	private String employeeId;

	@Column(name = "DEPARTMENT_NAME", columnDefinition="nvarchar(50)")
	private String departmentName;

	@Column(name = "PCC_CODE", columnDefinition="nvarchar(50)")
	private String pccCode;

	@Column(name = "PATHWAY", columnDefinition="nvarchar(50)")
	private String pathway;
	
	@Column(name = "TEMPLATE", columnDefinition="nvarchar(50)")
	private String template;	

	@Column(name = "PNP_CONTENT", columnDefinition="nvarchar(500)")
	private String pnpContent;
	
	@Column(name = "STATUS")
	private Boolean status;
	
	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getPathway() {
		return pathway;
	}

	public void setPathway(String pathway) {
		this.pathway = pathway;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getPnpContent() {
		return pnpContent;
	}

	public void setPnpContent(String pnpContent) {
		this.pnpContent = pnpContent;
	}

	public Long getId() {
		return id;
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
}
