package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 白名單維護帳號
 *
 * @author ???
 */
@Entity
@Table(name = "BCS_PNP_MAINTAIN_ACCOUNT")
public class PNPMaintainAccountModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    public static final String NORMAL_ACCOUNT = "Normal";
    public static final String UNICA_ACCOUNT = "Unica";

    /**
     * Id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * Normal Or Unica
     */
    @Column(name = "ACCOUNT_TYPE", columnDefinition = "nvarchar(50)")
    private String accountType;

    /**
     * 帳號
     */
    @Column(name = "ACCOUNT", columnDefinition = "nvarchar(50)")
    private String account;

    /**
     * 帳號屬性
     */
    @Column(name = "ACCOUNT_ATTRIBUTE", columnDefinition = "nvarchar(50)")
    private String accountAttribute;

    /**
     * 帳號類別
     */
    @Column(name = "ACCOUNT_CLASS", columnDefinition = "nvarchar(50)")
    private String accountClass;

    /**
     * 前方來源系統
     */
    @Column(name = "SOURCE_SYSTEM", columnDefinition = "nvarchar(50)")
    private String sourceSystem;

    /**
     * 單位代號
     */
    @Column(name = "DEPARTMENT_ID", columnDefinition = "nvarchar(50)")
    private String departmentId;

    /**
     * 員工編號
     */
    @Column(name = "EMPLOYEE_ID", columnDefinition = "nvarchar(50)")
    private String employeeId;

    /**
     * 處
     */
    @Column(name = "DIVISION_NAME", columnDefinition = "nvarchar(50)")
    private String divisionName;

    /**
     * 部
     */
    @Column(name = "DEPARTMENT_NAME", columnDefinition = "nvarchar(50)")
    private String departmentName;

    /**
     * 組
     */
    @Column(name = "GROUP_NAME", columnDefinition = "nvarchar(50)")
    private String groupName;

    /**
     *
     */
    @Column(name = "PCC_CODE", columnDefinition = "nvarchar(50)")
    private String pccCode;

    /**
     * 通路流
     */
    @Column(name = "PATHWAY", columnDefinition = "nvarchar(50)")
    private String pathway;

    /**
     * 樣板ID
     * Deprecated Desc:
     * 201909 改用單一樣板該欄位棄用，改用flexMainTitle, flexButtonName, flexButtonUrl設定樣板內容
     */
    @Deprecated
    @Column(name = "TEMPLATE", columnDefinition = "nvarchar(50)")
    private String template;

    /**
     * Flex 主標題
     */
//    @Column(name = "flex_title", columnDefinition = "nvarchar(20)")
    private String flexTitle;

    /**
     * Flex 按鈕文字JsonArray
     * [OK, Cancel]
     */
//    @Column(name = "flex_button_name", columnDefinition = "nvarchar(60)")
    private String flexButtonName;

    /**
     * Flex 按鈕URLJsonArray
     * ["http://www.goole.com", "http://www.google.com2"]
     */
//    @Column(name = "flex_button_url", columnDefinition = "nvarchar(600)")
    private String flexButtonUrl;

    /**
     * 簡訊內容
     */
    @Column(name = "PNP_CONTENT", columnDefinition = "nvarchar(500)")
    private String pnpContent;

    /**
     * 帳號狀態
     */
    @Column(name = "STATUS")
    private Boolean status;

    /**
     * 更新時間
     */
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 更新人員
     */
    @Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
    private String modifyUser;

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
