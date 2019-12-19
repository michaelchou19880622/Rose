package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
 * @author Alan
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
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
     * Flex樣板ID
     */
    @Column(name = "TEMPLATE", columnDefinition = "nvarchar(50)")
    private String template;

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
}
