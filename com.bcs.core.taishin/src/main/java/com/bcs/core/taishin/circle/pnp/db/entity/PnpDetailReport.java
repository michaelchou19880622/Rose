package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Pnp Detail Report Entity
 *
 * @author Alan
 */
@Getter
@Setter
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PNP_DETAIL_REPORT")
public class PnpDetailReport {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private String id;

    /**
     * 發送通路流
     */
    @Column(name = "PROCESS_FLOW")
    private String processFlow;
    /**
     * 最終發送通路流
     */
    @Column(name = "PROCESS_STAGE")

    private String processStage;
    /**
     * FTP 來源
     */
    @Column(name = "FTP_SOURCE")
    private String ftpSource;
    /**
     * 訊息內容
     */
    @Column(name = "MESSAGE")
    private String message;
    /**
     * 預約時間
     */
    @Column(name = "SCHEDULE_TIME")
    private Date scheduleTime;
    /**
     * Bc發送時間
     */
    @Column(name = "BC_TIME")
    private Date bcTime;
    /**
     * Pnp發送時間
     */
    @Column(name = "PNP_TIME")
    private Date pnpTime;
    /**
     * Sms發送時間
     */
    @Column(name = "SMS_TIME")
    private Date smsTime;
    /**
     * Bc發送狀態
     */
    @Column(name = "BC_STATUS")
    private String bcStatus;
    /**
     * Pnp發送狀態
     */
    @Column(name = "PNP_STATUS")
    private String pnpStatus;
    /**
     * Sms發送狀態
     */
    @Column(name = "SMS_STATUS")
    private String smsStatus;
    /**
     * Bc Http Status Code
     */
    @Column(name = "BC_HTTP_STATUS_CODE")
    private String bcHttpStatusCode;
    /**
     * Pnp Http Status Code
     */
    @Column(name = "PNP_HTTP_STATUS_CODE")
    private String pnpHttpStatusCode;
    /**
     * 行動電話號碼
     */
    @Column(name = "PHONE")
    private String phone;
    /**
     * PccCode
     */
    @Column(name = "PCC_CODE")
    private String pccCode;
    /**
     * 維護帳號
     */
    @Column(name = "ACCOUNT")
    private String account;
    /**
     * 前方來源系統
     */
    @Column(name = "SOURCE_SYSTEM")
    private String sourceSystem;
    /**
     * 員工代碼
     */
    @Column(name = "EMPLOYEE_ID")
    private String employeeId;
    /**
     * 處
     */
    @Column(name = "DIVISION_NAME")
    private String divisionName;

    /**
     * 部
     */
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    /**
     * 組
     */
    @Column(name = "GROUP_NAME")
    private String groupName;
    /**
     * 建立時間
     */
    @Column(name = "CREATE_TIME")
    private Date createTime;
    /**
     * 發送廠商訊息流水號
     */
    @Column(name = "SN")
    private String sn;
    /**
     * 更新時間
     */
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;
    /**
     * 訊息樣板ID
     */
    @Column(name = "TEMPLATE")
    private String template;
    /**
     * Detail Id
     */
    @Column(name = "DETAIL_ID")
    private String detailId;
    /**
     * Main Id
     */
    @Column(name = "MAIN_ID")
    private String mainId;
    /**
     * 訊息內文點數
     */
    @Column(name = "MESSAGE_POINT")
    private Integer messagePoint;
    /**
     * 行銷活動代碼
     */
    @Column(name = "CAMPAIGN_ID")
    private String campaignId;
    /**
     * 行銷活動階段
     */
    @Column(name = "SEGMENT_ID")
    private String segmentId;
    /**
     * 行銷活動客群代碼
     */
    @Column(name = "PROGRAM_ID")
    private String programId;
    /**
     * 客戶ID
     */
    @Column(name = "PID")
    private String pid;
    /**
     * Line UID
     */
    @Column(name = "UID")
    private String uid;
    /**
     * 是否為國際簡訊
     */
    @Column(name = "IS_INTERNATIONAL")
    private Integer isInternational;
}
