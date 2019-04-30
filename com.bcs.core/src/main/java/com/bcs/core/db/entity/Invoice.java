package com.bcs.core.db.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_INVOICE", indexes = {})
public class Invoice extends AbstractBcsEntity{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "INVOICE_ID", columnDefinition="NVARCHAR(50)")
    private String invoiceId = null;

    @Column(name = "INV_NUM", columnDefinition="NVARCHAR(10)")
    private String invNum = null;

    @Column(name = "INV_DATE")
    private Date invDate = null;

    @Column(name = "SELLER_NAME", columnDefinition="NVARCHAR(100)")
    private String sellerName = null;

    @Column(name = "INV_STATUS", columnDefinition="NVARCHAR(10)")
    private String invStatus = null;

    @Column(name = "INV_PERIOD", columnDefinition="NVARCHAR(5)")
    private String invPeriod = null;

    @Column(name = "SELLER_BAN", columnDefinition="NVARCHAR(8)")
    private String sellerBan = null;

    @Column(name = "SELLER_ADDRESS", columnDefinition="NVARCHAR(200)")
    private String sellerAddress = null;

    @Column(name = "V", columnDefinition="NVARCHAR(5)")
    private String v = null;

    @Column(name = "CODE", columnDefinition="NVARCHAR(3)")
    private String code = null;

    @Column(name = "MSG", columnDefinition="NVARCHAR(50)")
    private String msg = null;

    @Column(name = "MID", columnDefinition="NVARCHAR(50)")
    private String mid = null;

    @Column(name = "UPLOAD_TIME")
    private Date uploadTime = null;

    @Column(name = "PIC_PATH", columnDefinition="NVARCHAR(100)")
    private String picPath = null;

    @Column(name = "CAMPAIGN_ID", columnDefinition="NVARCHAR(50)")
    private String campaignId = null;

    @Column(name = "STATUS", columnDefinition="NVARCHAR(50)")
    private String status = null;

    @Column(name = "PRIZE_LIST_ID")
    private Long prizeListId;

    @Column(name = "INV_TERM", columnDefinition="NVARCHAR(7)")
    private String invTerm = null;

    @Column(name = "RANDOM_NUMBER", columnDefinition="NVARCHAR(4)")
    private String randomNumber = null;
    
    @Transient
    private List<InvoiceDetail> invoiceDetails = null;

    public Invoice() {
        
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvNum() {
        return invNum;
    }

    public void setInvNum(String invNum) {
        this.invNum = invNum;
    }

    public Date getInvDate() {
        return invDate;
    }

    public void setInvDate(Date invDate) {
        this.invDate = invDate;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getInvStatus() {
        return invStatus;
    }

    public void setInvStatus(String invStatus) {
        this.invStatus = invStatus;
    }

    public String getInvPeriod() {
        return invPeriod;
    }

    public void setInvPeriod(String invPeriod) {
        this.invPeriod = invPeriod;
    }

    public String getSellerBan() {
        return sellerBan;
    }

    public void setSellerBan(String sellerBan) {
        this.sellerBan = sellerBan;
    }

    public String getSellerAddress() {
        return sellerAddress;
    }

    public void setSellerAddress(String sellerAddress) {
        this.sellerAddress = sellerAddress;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public List<InvoiceDetail> getInvoiceDetails() {
        return invoiceDetails;
    }

    public void setInvoiceDetails(List<InvoiceDetail> invoiceDetails) {
        this.invoiceDetails = invoiceDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPrizeListId() {
        return prizeListId;
    }

    public void setPrizeListId(Long prizeListId) {
        this.prizeListId = prizeListId;
    }

    public String getInvTerm() {
        return invTerm;
    }

    public void setInvTerm(String invTerm) {
        this.invTerm = invTerm;
    }

    public String getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(String randomNumber) {
        this.randomNumber = randomNumber;
    }
}
