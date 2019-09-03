package com.bcs.core.taishin.circle.db.entity;

import com.bcs.core.json.AbstractBcsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 台新人事資訊
 * @author ???
 */
@Entity
@Table(name = "BCS_TAISHIN_EMPLOYEE")
public class TaishinEmployee extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * 員工編號
     */
    @Column(name = "EMPLOYEE_ID", columnDefinition = "nvarchar(100)")
    private String employeeId;

    /**
     * 單位代號
     */
    @Column(name = "DEPARTMENT_ID", columnDefinition = "nvarchar(100)")
    private String departmentId;

    /**
     * 處室名稱
     */
    @Column(name = "DIVISION_NAME", columnDefinition = "nvarchar(100)")
    private String divisionName;

    /**
     * 部門名稱
     */
    @Column(name = "DEPARTMENT_NAME", columnDefinition = "nvarchar(100)")
    private String departmentName;

    /**
     * 組別名稱
     */
    @Column(name = "GROUP_NAME", columnDefinition = "nvarchar(100)")
    private String groupName;

    /**
     *
     */
    @Column(name = "EASY_NAME", columnDefinition = "nvarchar(100)")
    private String easyName;

    /**
     *
     */
    @Column(name = "PCC_CODE", columnDefinition = "nvarchar(100)")
    private String pccCode;

    /**
     * 更新時間
     */
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 更新人
     */
    @Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
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

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void getDepartmentName(String departmentName) {
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

    public String getDepartmentName() {
        return departmentName;
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

    public String getEasyName() {
        return easyName;
    }

    public void setEasyName(String easyName) {
        this.easyName = easyName;
    }
}
