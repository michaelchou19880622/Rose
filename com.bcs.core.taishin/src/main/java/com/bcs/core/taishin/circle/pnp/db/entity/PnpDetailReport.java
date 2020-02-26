package com.bcs.core.taishin.circle.pnp.db.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private long id;

    /**
     * 發送通路流
     */
    @Column(name = "PROCESS_FLOW", columnDefinition = "CHAR(3)")
    private String processFlow;

    /**
     * 最終發送通路流
     */
    @Column(name = "PROCESS_STAGE", columnDefinition = "CHAR(3)")
    private String processStage;

    /**
     * FTP 來源
     */
    @Column(name = "FTP_SOURCE", columnDefinition = "VARCHAR(10)")
    private String ftpSource;

    /**
     * 訊息內容
     */
    @Column(name = "MESSAGE", columnDefinition = "NVARCHAR(1000)")
    private String message;

    /**
     * 預約時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "SCHEDULE_TIME")
    private Date scheduleTime;

    /**
     * Bc發送時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "BC_TIME")
    private Date bcTime;

    /**
     * Pnp發送時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "PNP_TIME")
    private Date pnpTime;

    /**
     * Sms發送時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "SMS_TIME")
    private Date smsTime;

    /**
     * Bc發送狀態
     */
    @Column(name = "BC_STATUS", columnDefinition = "VARCHAR(50)")
    private String bcStatus;

    /**
     * Pnp發送狀態
     */
    @Column(name = "PNP_STATUS", columnDefinition = "VARCHAR(50)")
    private String pnpStatus;

    /**
     * Sms發送狀態
     */
    @Column(name = "SMS_STATUS", columnDefinition = "VARCHAR(50)")
    private String smsStatus;

    /**
     * Bc Http Status Code
     */
    @Column(name = "BC_HTTP_STATUS_CODE", columnDefinition = "CHAR(3)")
    private String bcHttpStatusCode;

    /**
     * Pnp Http Status Code
     */
    @Column(name = "PNP_HTTP_STATUS_CODE", columnDefinition = "CHAR(3)")
    private String pnpHttpStatusCode;

    /**
     * 行動電話號碼
     */
    @Column(name = "PHONE", columnDefinition = "CHAR(15)")
    private String phone;

    /**
     * PccCode
     */
    @Column(name = "PCC_CODE", columnDefinition = "CHAR(10)")
    private String pccCode;

    /**
     * 維護帳號
     */
    @Column(name = "ACCOUNT", columnDefinition = "VARCHAR(30)")
    private String account;

    /**
     * 銀行前方來源系統
     */
    @Column(name = "SOURCE_SYSTEM", columnDefinition = "VARCHAR(20)")
    private String sourceSystem;

    /**
     * 員工代碼
     */
    @Column(name = "EMPLOYEE_ID", columnDefinition = "VARCHAR(30)")
    private String employeeId;

    /**
     * 處
     */
    @Column(name = "DIVISION_NAME", columnDefinition = "NVARCHAR(50)")
    private String divisionName;


    /**
     * 部
     */
    @Column(name = "DEPARTMENT_NAME", columnDefinition = "NVARCHAR(50)")
    private String departmentName;


    /**
     * 組
     */
    @Column(name = "GROUP_NAME", columnDefinition = "NVARCHAR(50)")
    private String groupName;

    /**
     * 建立時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Date createTime;

    /**
     * 發送廠商訊息流水號
     */
    @Column(name = "SN", columnDefinition = "VARCHAR(50)")
    private String sn;

    /**
     * 更新時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 訊息樣板ID
     */
    @Column(name = "TEMPLATE_ID")
    private Long templateId;

    /**
     * Detail Id
     */
    @Column(name = "DETAIL_ID")
    private Long detailId;

    /**
     * Main Id
     */
    @Column(name = "MAIN_ID")
    private Long mainId;

    /**
     * 訊息內文點數
     */
    @Column(name = "MESSAGE_POINT")
    private Integer messagePoint;

    /**
     * 行銷活動代碼
     */
    @Column(name = "CAMPAIGN_ID", columnDefinition = "VARCHAR(50)")
    private String campaignId;

    /**
     * 行銷活動階段
     */
    @Column(name = "SEGMENT_ID", columnDefinition = "VARCHAR(50)")
    private String segmentId;

    /**
     * 行銷活動客群代碼
     */
    @Column(name = "PROGRAM_ID", columnDefinition = "VARCHAR(50)")
    private String programId;

    /**
     * 客戶ID
     */
    @Column(name = "PID", columnDefinition = "VARCHAR(64)")
    private String pid;

    /**
     * Line UID
     */
    @Column(name = "UID", columnDefinition = "CHAR(33)")
    private String uid;

    /**
     * 是否為國際簡訊
     */
    @Column(name = "IS_INTERNATIONAL")
    private Integer isInternational;
}
