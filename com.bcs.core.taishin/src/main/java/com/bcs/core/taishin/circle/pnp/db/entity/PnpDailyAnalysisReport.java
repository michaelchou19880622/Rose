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
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Alan
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "PNP_DAILY_ANALYSIS_REPORT", indexes = {
        @Index(name = "", columnList = "", unique = false)
})
public class PnpDailyAnalysisReport {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private String id;

    /**
     * 日報日期
     */
    private Date reportDate;

    /**
     * 發送日期(文字格式為YYYYMMDD)
     */
    @NotNull
    @Column(name = "DELIVERY_DATE")
    private Date deliveryDate;

    /**
     * 處
     */
    @Column(name = "DEPT", columnDefinition = "VARCHAR2(30)")
    private String dept;

    /**
     * 部
     */
    @Column(name = "DIV", columnDefinition = "VARCHAR2(30)")
    private String div;

    /**
     * 組
     */
    @Column(name = "ORG", columnDefinition = "VARCHAR2(30)")
    private String org;

    /**
     * 前方來源系統
     */
    @Column(name = "FRONTEND_SYSTEM", columnDefinition = "VARCHAR2(30)")
    private String frontEndSystem;

    /**
     * 發送帳號
     */
    @Column(name = "ACCOUNT", columnDefinition = "VARCHAR2(30)")
    private String account;

    /**
     * 掛帳PccCode(10碼)
     */
    @Column(name = "ACCOUNT_PCCCODE", columnDefinition = "VARCHAR2(10)")
    private String accountPccCode;

    /**
     * 訊息樣板
     */
    @Column(name = "MESSAGE_TEMPLATE", columnDefinition = "VARCHAR2(30)")
    private String messageTemplate;

    /**
     * 通路流
     */
    @Column(name = "DELIVERY_PATHWAY", columnDefinition = "VARCHAR2(200)")
    private String deliveryPathWay;

    /**
     * 訊息內文長度
     */
    @Column(name = "MESSAGE_LENGTH", columnDefinition = "NUMBER(10)")
    private int messageLength;

    /**
     * 訊息內文
     */
    @Column(name = "MESSAGE", columnDefinition = "NVARCHAR2(700)")
    private String message;

    /**
     * 行銷活動代碼
     */
    @Column(name = "CAMPAIGN_ID", columnDefinition = "VARCHAR2(30)")
    private String campaignId;

    /**
     * SMS發送通路
     */
    @Column(name = "CHANNEL_SMS", columnDefinition = "VARCHAR2(30)")
    private String smsChannel;

    /**
     * SMS總發送數
     */
    @Column(name = "CHANNEL_TOTAL_MESSAGE_SMS", columnDefinition = "NUMBER(10)")
    private int smsChannelTotalMessage;

    /**
     * SMS成功數
     */
    @Column(name = "SUCCESS_MESSAGE_SMS", columnDefinition = "NUMBER(10)")
    private int smsSuccessMessage;

    /**
     * SMS失敗數
     */
    @Column(name = "FAILED_MESSAGE_SMS", columnDefinition = "NUMBER(10)")
    private int smsFailMessage;

    /**
     * //FIXME 什麼是成功點數
     * SMS成功點數
     */
    @Column(name = "SUCCESS_MESSAGE_POINT_SMS", columnDefinition = "NUMBER(10)")
    private int smsSuccessMessagePoint;

    /**
     * SMS成功率
     */
    @Column(name = "SUCCESS_RATE_SMS", columnDefinition = "NUMBER(10)")
    private int smsSuccessRate;

    /**
     * PNP發送通路
     */
    @Column(name = "CHANNEL_PNP", columnDefinition = "VARCHAR2(30)")
    private String pnpChannel;

    /**
     * PNP總發送數
     */
    @Column(name = "CHANNEL_TOTAL_MESSAGE_PNP", columnDefinition = "NUMBER(10)")
    private int pnpChannelTotalMessage;

    /**
     * PNP成功數
     */
    @Column(name = "SUCCESS_MESSAGE_PNP", columnDefinition = "NUMBER(10)")
    private int pnpSuccessMessage;

    /**
     * PNP失敗數
     */
    @Column(name = "FAILED_MESSAGE_PNP", columnDefinition = "NUMBER(10)")
    private int pnpFailMessage;

    /**
     * PNP成功率
     */
    @Column(name = "SUCCESS_RATE_PNP", columnDefinition = "NUMBER(10)")
    private int pnpSuccessRate;

    /**
     * BC發送通路
     */
    @Column(name = "CHANNEL_BC", columnDefinition = "")
    private String bcChannel;

    /**
     * BC總發送數
     */
    @Column(name = "CHANNEL_TOTAL_MESSAGE_BC", columnDefinition = "NUMBER(10)")
    private int bcChannelTotalMessage;

    /**
     * BC成功數
     */
    @Column(name = "SUCCESS_MESSAGE_BC", columnDefinition = "NUMBER(10)")
    private int bcSuccessMessage;

    /**
     * BC失敗數
     */
    @Column(name = "FAILED_MESSAGE_BC", columnDefinition = "NUMBER(10)")
    private int bcFailMessage;

    /**
     * BC成功率
     */
    @Column(name = "SUCCESS_RATE_BC", columnDefinition = "NUMBER(10)")
    private int bcSuccessRate;

    /**
     * 總發送數
     */
    @Column(name = "TOTAL_MESSAGE", columnDefinition = "NUMBER(10)")
    private int totalMessage;

    /**
     * 建立日期 文字格式(YYYYMMDD)
     */
    @Column(name = "CREATE_DATE")
    private String createDate;

    /**
     * 更新日期 文字格式(YYYYMMDD)
     */
    @Column(name = "UPDATE_DATE")
    private String aa;

}
