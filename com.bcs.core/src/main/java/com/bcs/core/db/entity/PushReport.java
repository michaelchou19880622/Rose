package com.bcs.core.db.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "BCS_PUSH_REPORT",
indexes = {
	       @Index(name = "INDEX_0", columnList = "REPORT_LINK_ID"),
	       @Index(name = "INDEX_1", columnList = "REPORT_MSG_SEND_ID"),
	       @Index(name = "INDEX_2", columnList = "REPORT_RICH_ID"),
	})
public class PushReport extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "REPORT_ID")
	private Long reportId;

	@Column(name = "REPORT_LINK_ID", columnDefinition="nvarchar(40)")
	private String reportLinkId;
	
	@Column(name = "REPORT_RICH_ID", columnDefinition="nvarchar(40)")
	private String reportRichId;
	
	@Column(name = "REPORT_MSG_SEND_ID")
	private Long reportMsgSendId;
	
	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "REPORT_PUSH_TIME")
	private Date reportPushTime;

	@Column(name = "REPORT_PUSH_TYPE", columnDefinition="nvarchar(20)")
	private String reportPushType;
	
	@Column(name = "REPORT_IMAGE_ID", columnDefinition="nvarchar(40)")
	private String reportImageId;

	@Column(name = "REPORT_PUSH_TXT", columnDefinition="nvarchar(500)")
	private String reportPushTxt;
	
	@Column(name = "REPORT_PUSH_NUMBER")
	private Long reportPushNumber;
	
	@Column(name = "REPORT_PUSH_URL_CLICK")
	private Long reportPushUrlClick;
	
	@Column(name = "REPORT_PUSH_TOTAL_CLICK")
	private Long reportPushTotalClick;
	
	@Column(name = "REPORT_PUSH_CTR_VALUE")
	private BigDecimal reportPushCtrValue;
	
	@Column(name = "REPORT_PUSH_URL", columnDefinition="nvarchar(500)")
	private String reportPushUrl;
	
	@Column(name = "REPORT_PUSH_TRACKING_CODE", columnDefinition="nvarchar(100)")
	private String reportPushTrackingCode;
	
	@Column(name = "REPORT_PUSH_URL_CLICK_THROUGH")
	private Long reportPushUrlClickThrough;
	
	@Column(name = "REPORT_PUSH_TOTAL_CLICK_THROUGH")
	private Long reportPushTotalClickThrough;
	
	@Column(name = "REPORT_PUSH_URL_VISIT")
	private Long reportPushUrlVisit;
	
	@Column(name = "REPORT_PUSH_TOTAL_VISIT")
	private Long reportPushTotalVisit;
	
	@Column(name = "REPORT_PUSH_URL_PRODUCT_VIEW")
	private Long reportPushUrlProductView;
	
	@Column(name = "REPORT_PUSH_TOTAL_PRODUCT_VIEW")
	private Long reportPushTotalProductView;
	
	@Column(name = "REPORT_PUSH_URL_DEEP")
	private BigDecimal reportPushUrlDeep;
	
	@Column(name = "REPORT_PUSH_TOTAL_DEEP")
	private BigDecimal reportPushTotalDeep;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getReportLinkId() {
		return reportLinkId;
	}

	public void setReportLinkId(String reportLinkId) {
		this.reportLinkId = reportLinkId;
	}

	public String getReportRichId() {
		return reportRichId;
	}

	public void setReportRichId(String reportRichId) {
		this.reportRichId = reportRichId;
	}

	public Long getReportMsgSendId() {
		return reportMsgSendId;
	}

	public void setReportMsgSendId(Long reportMsgSendId) {
		this.reportMsgSendId = reportMsgSendId;
	}

	public Date getReportPushTime() {
		return reportPushTime;
	}

	public void setReportPushTime(Date reportPushTime) {
		this.reportPushTime = reportPushTime;
	}

	public String getReportPushType() {
		return reportPushType;
	}

	public void setReportPushType(String reportPushType) {
		this.reportPushType = reportPushType;
	}

	public String getReportImageId() {
		return reportImageId;
	}

	public void setReportImageId(String reportImageId) {
		this.reportImageId = reportImageId;
	}

	public String getReportPushTxt() {
		return reportPushTxt;
	}

	public void setReportPushTxt(String reportPushTxt) {
		this.reportPushTxt = reportPushTxt;
	}

	public Long getReportPushNumber() {
		return reportPushNumber;
	}

	public void setReportPushNumber(Long reportPushNumber) {
		this.reportPushNumber = reportPushNumber;
	}

	public Long getReportPushUrlClick() {
		return reportPushUrlClick;
	}

	public void setReportPushUrlClick(Long reportPushUrlClick) {
		this.reportPushUrlClick = reportPushUrlClick;
	}

	public Long getReportPushTotalClick() {
		return reportPushTotalClick;
	}

	public void setReportPushTotalClick(Long reportPushTotalClick) {
		this.reportPushTotalClick = reportPushTotalClick;
	}

	public BigDecimal getReportPushCtrValue() {
		return reportPushCtrValue;
	}

	public void setReportPushCtrValue(BigDecimal reportPushCtrValue) {
		this.reportPushCtrValue = reportPushCtrValue;
	}

	public String getReportPushUrl() {
		return reportPushUrl;
	}

	public void setReportPushUrl(String reportPushUrl) {
		this.reportPushUrl = reportPushUrl;
	}

	public String getReportPushTrackingCode() {
		return reportPushTrackingCode;
	}

	public void setReportPushTrackingCode(String reportPushTrackingCode) {
		this.reportPushTrackingCode = reportPushTrackingCode;
	}

	public Long getReportPushUrlClickThrough() {
		return reportPushUrlClickThrough;
	}

	public void setReportPushUrlClickThrough(Long reportPushUrlClickThrough) {
		this.reportPushUrlClickThrough = reportPushUrlClickThrough;
	}

	public Long getReportPushTotalClickThrough() {
		return reportPushTotalClickThrough;
	}

	public void setReportPushTotalClickThrough(Long reportPushTotalClickThrough) {
		this.reportPushTotalClickThrough = reportPushTotalClickThrough;
	}

	public Long getReportPushUrlVisit() {
		return reportPushUrlVisit;
	}

	public void setReportPushUrlVisit(Long reportPushUrlVisit) {
		this.reportPushUrlVisit = reportPushUrlVisit;
	}

	public Long getReportPushTotalVisit() {
		return reportPushTotalVisit;
	}

	public void setReportPushTotalVisit(Long reportPushTotalVisit) {
		this.reportPushTotalVisit = reportPushTotalVisit;
	}

	public Long getReportPushUrlProductView() {
		return reportPushUrlProductView;
	}

	public void setReportPushUrlProductView(Long reportPushUrlProductView) {
		this.reportPushUrlProductView = reportPushUrlProductView;
	}

	public Long getReportPushTotalProductView() {
		return reportPushTotalProductView;
	}

	public void setReportPushTotalProductView(Long reportPushTotalProductView) {
		this.reportPushTotalProductView = reportPushTotalProductView;
	}

	public BigDecimal getReportPushUrlDeep() {
		return reportPushUrlDeep;
	}

	public void setReportPushUrlDeep(BigDecimal reportPushUrlDeep) {
		this.reportPushUrlDeep = reportPushUrlDeep;
	}

	public BigDecimal getReportPushTotalDeep() {
		return reportPushTotalDeep;
	}

	public void setReportPushTotalDeep(BigDecimal reportPushTotalDeep) {
		this.reportPushTotalDeep = reportPushTotalDeep;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
}
