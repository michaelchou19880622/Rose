package com.bcs.core.taishin.circle.pnp.db.entity;

import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author Alan
 * @apiNote 20200230:{
 * id:001, deliveryDate:20200230, dept:A01, div:B01, org:C01, frontendSystem: mitake, account: DEPOSIT_1344_AA, accountPccCode:1234567899, msgTemplate: T01, deliveryPathWay: PNP, msgLength: 23, msg:asdfghjkl;qwertyuiop, campaignId:campaign123, smsChannel:mitake,
 * <p>
 * <p>
 * }
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
//@Entity
//@Table(name = "PNP_DAILY_ANALYSIS_REPORT", indexes = {
//        @Index(name = "", columnList = "", unique = false)
//})
public class PnpDailyAnalysisReport {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private String id;

    // TODO ---------------------- 群組資訊 --------------------------
    /**
     * 發送日期 文字格式(YYYYMMDD)
     */
    @NotNull
    @JsonFormat(pattern = "yyyyMMdd")
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
    private String msgTemplate;

    /**
     * 通路流
     */
    @Column(name = "DELIVERY_PATHWAY", columnDefinition = "VARCHAR2(200)")
    private String deliveryPathWay;

    /**
     * 訊息內文長度
     */
    @Column(name = "MESSAGE_LENGTH", columnDefinition = "NUMBER(10)")
    private int msgLength;

    /**
     * 訊息內文
     */
    @Column(name = "MESSAGE", columnDefinition = "NVARCHAR2(700)")
    private String msg;

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

    //TODO------------------------- 統計資訊 --------------------------------------
    /**
     * SMS總發送數
     */
    @Column(name = "CHANNEL_TOTAL_MESSAGE_SMS", columnDefinition = "NUMBER(10)")
    private double smsChannelTotalMsgCount;

    /**
     * SMS成功數
     */
    @Column(name = "SUCCESS_MESSAGE_SMS", columnDefinition = "NUMBER(10)")
    private double smsSuccessMsgCount;

    /**
     * SMS失敗數
     */
    @Column(name = "FAILED_MESSAGE_SMS", columnDefinition = "NUMBER(10)")
    private double smsFailMsgCount;

    /**
     * //FIXME 什麼是成功點數
     * SMS成功點數
     */
    @Column(name = "SUCCESS_MESSAGE_POINT_SMS", columnDefinition = "NUMBER(10)")
    private double smsSuccessMsgPoint;

    /**
     * SMS成功率
     */
    @Column(name = "SUCCESS_RATE_SMS", columnDefinition = "NUMBER(10)")
    private double smsSuccessRate;

    /**
     * PNP發送通路
     */
    @Column(name = "CHANNEL_PNP", columnDefinition = "VARCHAR2(30)")
    private String pnpChannel;

    /**
     * PNP總發送數
     */
    @Column(name = "CHANNEL_TOTAL_MESSAGE_PNP", columnDefinition = "NUMBER(10)")
    private double pnpChannelTotalMsgCount;

    /**
     * PNP成功數
     */
    @Column(name = "SUCCESS_MESSAGE_PNP", columnDefinition = "NUMBER(10)")
    private double pnpSuccessMsgCount;

    /**
     * PNP失敗數
     */
    @Column(name = "FAILED_MESSAGE_PNP", columnDefinition = "NUMBER(10)")
    private double pnpFailMsgCount;

    /**
     * PNP成功率
     */
    @Column(name = "SUCCESS_RATE_PNP", columnDefinition = "NUMBER(10)")
    private double pnpSuccessRate;

    /**
     * BC發送通路
     */
    @Column(name = "CHANNEL_BC", columnDefinition = "")
    private String bcChannel;

    /**
     * BC總發送數
     */
    @Column(name = "CHANNEL_TOTAL_MESSAGE_BC", columnDefinition = "NUMBER(10)")
    private double bcChannelTotalMsgCount;

    /**
     * BC成功數
     */
    @Column(name = "SUCCESS_MESSAGE_BC", columnDefinition = "NUMBER(10)")
    private double bcSuccessMsgCount;

    /**
     * BC失敗數
     */
    @Column(name = "FAILED_MESSAGE_BC", columnDefinition = "NUMBER(10)")
    private double bcFailMsgCount;

    /**
     * BC成功率
     */
    @Column(name = "SUCCESS_RATE_BC", columnDefinition = "NUMBER(10)")
    private double bcSuccessRate;

    /**
     * 總發送數
     */
    @Column(name = "TOTAL_MESSAGE", columnDefinition = "NUMBER(10)")
    private double totalMsg;

    /**
     * 建立日期 文字格式(YYYYMMDD)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "CREATE_DATE")
    private Date createDate;

    /**
     * 更新日期 文字格式(YYYYMMDD)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "UPDATE_DATE")
    private Date updateDate;

    /**
     * Convert detail list to analysis report
     */
    public PnpDailyAnalysisReport convertDetailListToRecord(List<PnpDetail> detailList) {
        //TODO
        detailList.forEach(d -> {
            d.getBcStatus().equals(PnpStatusEnum.BC_SENT_COMPLETE.value);
            d.getBcStatus().equals(PnpStatusEnum.BC_SENT_COMPLETE.value);
            d.getBcStatus().equals(PnpStatusEnum.BC_SENT_COMPLETE.value);
        });
        return null;
    }

    /**
     * bc成功率
     */
    public double getBcSuccessRate() {
        return (this.bcSuccessMsgCount / this.bcChannelTotalMsgCount) * 100;
    }

    /**
     * bc失敗率
     */
    public double getBcFailRate() {
        return (this.bcFailMsgCount / this.bcChannelTotalMsgCount) * 100;
    }

    /**
     * pnp成功率
     */
    public double getPnpSuccessRate() {
        return (this.pnpSuccessMsgCount / this.pnpChannelTotalMsgCount) * 100;
    }

    /**
     * pnp失敗率
     */
    public double getPnpFailRate() {
        return (this.pnpFailMsgCount / this.pnpChannelTotalMsgCount) * 100;
    }

    /**
     * sms成功率
     */
    public double getSmsSuccessRate() {
        return (this.smsSuccessMsgCount / this.smsChannelTotalMsgCount) * 100;
    }

    /**
     * sms失敗率
     */
    public double getSmsFailRate() {
        return (this.smsFailMsgCount / this.smsChannelTotalMsgCount) * 100;
    }
}
