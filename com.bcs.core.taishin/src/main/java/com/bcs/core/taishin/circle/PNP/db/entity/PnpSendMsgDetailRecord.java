package com.bcs.core.taishin.circle.PNP.db.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;



/**
 * 
欄位名稱                                                                                                欄位型態                                   欄位說明                                      說明                                                                                    PRIMARY KEY / INDEX        資料範例                         NOT NULL (必填欄位)
RECORD_ID                        nvarchar(128)        序號 資料ID           自己定義(main id、detail id)        PRIMARY KEY                 0                Y
FRONTEND_SYSTEM                  nvarchar(30)        前方來源系統                                     檔名                                                                                                                                                                   PRMSMS              Y
DELIVERY_PATHWAY                 nvarchar(200)        通路流                                            後台設定檢核(白名單)                                          BC->PNP->SMS          Y
CHANNEL                          nvarchar(30)        發送通路                                                                                                                                            INDEX_CHANNEL             SMS_三竹                                      Y
ACCOUNT                          nvarchar(30)        發送帳號                                                                                                                                 INDEX_CHANNEL_ACCOUNT           eTag             Y
ACCOUNT_PCCCODE                  nvarchar(10)        掛帳PccCode (10碼)      後台設定檢核(白名單)                                          601259030          Y
BATCH_NO                         nvarchar(64)        "發送廠商訊息批次代碼"       main id                      INDEX_BATCH_NO        ef687024456f7a2b2fbda9a63e1fc89946f6df36375715ec501dcf547ae5076c        Y
SERIAL_NO                        nvarchar(64)        "發送廠商訊息流水號"        detail id                   INDEX_SERIAL_NO        79734c01d723c769d27ea9f4ba61f9b09aa0e52ca7d261f199373ba66f5dbbaa        Y
MESSAGE_TEMPLATE                 nvarchar(30)        訊息樣板                                                                                                                                                                                                                             A0001        
MESSAGE                          nvarchar(700)        訊息內文                                                                                                                                                                                                                          台新貴賓您好                               Y
MESSAGE_POINT                    NUMBER(10)          訊息內文點數                                發送比數(都帶1)                                                      1             Y
CAMPAIGN_ID                      nvarchar(30)        行銷活動代碼                                互動(Body-CampaignID)，若無則空白                                                                                                             1090313        Y
PROGR_CD                         nvarchar(20)        行銷活動階段                                互動(Body-SegmentID)，若無則空白                                                                                                                          3132        Y
SEG_CD                           nvarchar(10)        行銷活動客群代碼                            互動(Body-ProgramID) ，若無則空白                                                                                                                    1001        Y
CUSTOMER_ID                      nvarchar(64)        客戶ID                身分證字號(先帶空白)                d1d51a4ce43a970ff1b23e45a50a5b4a58ed45a8b37fe1e551385f9997f470e0        Y
CUSTOMER_CellPhone_Number        nvarchar(64)        客戶手機號碼                                                        d1d51a4ce43a970ff1b23e45a50a5b4a58ed45a8b37fe1e551385f9997f470e0        Y
Lin_BC_UUID                                          Line BC UUID              BC發送才會有                        
SCHEDULE_DATE                    nvarchar(18)        預約日期 (YYYYMMDD)         檔案上                                                                                                                                                               20190311         Y
SCHEDULE_TIME                    nvarchar(18)        預約時間 (HHmmss)           檔案上                                                                                                                                                              182220         
DELIVERY_DATE                    nvarchar(18)        發送日期 (YYYYMMDD)         實際                                                                     INDEX_DELIVERY_DATE             20190311         Y
DELIVERY_TIME                    nvarchar(18)        發送時間 (HHmmss)           實際                                                                                                                                                                           182220         
STATUS_CODE                      NUMBER(10)          發送狀態_代碼                                            200                                                             200        Y
STATUS_INFO                      nvarchar(30)        發送狀態_中文說明                                                                                                                                                                                                                                         已送達手機        Y
STATUS_SUCCESS                   NUMBER(1)          發送狀態_成功與否 (1: 成功 / 2: 失敗)       SMS要去簡訊DB撈成功失敗                                                                                                                     1        Y
CREATE_DATE                      nvarchar(8)        資料建立日期 (YYYYMMDD)                                                                  20190311        Y
UPDATE_DATE                      nvarchar(8)        資料更新日期 (YYYYMMDD)                                                                  20190311        Y
                                                                                                                                                                 

 * 
 * @author Kenneth
 *
 */

@Entity
@Table(name = "BCS_PNP_SEND_MSG_DETAIL_RECORD",
indexes = {
	       @Index(name = "INDEX_0", columnList = "ID"),
	       @Index(name = "INDEX_1", columnList = "CHANNEL"),
	       @Index(name = "INDEX_2", columnList = "ACCOUNT"),
	       @Index(name = "INDEX_3", columnList = "BATCH_NO"),
	       @Index(name = "INDEX_4", columnList = "SERIAL_NO"),
	       @Index(name = "INDEX_5", columnList = "DELIVERY_DATE")
	})
public class PnpSendMsgDetailRecord extends PnpDetail {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "RECORD_ID"                        , columnDefinition="nvarchar(128)") 
	private String recordId;
	@Column(name = "FRONTEND_SYSTEM"                  , columnDefinition="nvarchar(30) ") 
	private String frontendSystem;
	@Column(name = "DELIVERY_PATHWAY"                 , columnDefinition="nvarchar(200)") 
	private String deliveryPathway;
	@Column(name = "CHANNEL"                          , columnDefinition="nvarchar(30) ") 
	private String channel;
	@Column(name = "ACCOUNT"                          , columnDefinition="nvarchar(30) ") 
	private String account;
	@Column(name = "ACCOUNT_PCCCODE"                  , columnDefinition="nvarchar(10) ") 
	private String accountPccCode;
	@Column(name = "BATCH_NO"                         , columnDefinition="nvarchar(64) ") 
	private String batchNo;
	@Column(name = "SERIAL_NO"                        , columnDefinition="nvarchar(64) ")
	private String serialNo;
	@Column(name = "MESSAGE_TEMPLATE"                 , columnDefinition="nvarchar(30) ")
	private String messageTemplate;
	@Column(name = "MESSAGE"                          , columnDefinition="nvarchar(700)")
	private String message;
	@Column(name = "MESSAGE_POINT"                    , columnDefinition="nvarchar(10) ")
	private int messagePoint;
	@Column(name = "CAMPAIGN_ID"                      , columnDefinition="nvarchar(30) ")
	private String campaignId; 
	@Column(name = "PROGR_CD"                         , columnDefinition="nvarchar(20) ")
	private String progrCd;
	@Column(name = "SEG_CD"                           , columnDefinition="nvarchar(10) ") 
	private String segCd;
	@Column(name = "CUSTOMER_ID"                      , columnDefinition="nvarchar(64) ") 
	private String customerId;
	@Column(name = "CUSTOMER_CellPhone_Number"        , columnDefinition="nvarchar(64) ") 
	private String customerCellPhoneNumber;
	@Column(name = "Lin_BC_UUID"                      , columnDefinition="nvarchar(50) ") 
	private String lineBcUUID;
	@Column(name = "SCHEDULE_DATE"                    , columnDefinition="nvarchar(18) ") 
	private String scheduleDate;
	@Column(name = "SCHEDULE_TIME"                    , columnDefinition="nvarchar(18) ") 
	private String scheduleTime;
	@Column(name = "DELIVERY_DATE"                    , columnDefinition="nvarchar(18) ") 
	private String deliveryDate;
	@Column(name = "DELIVERY_TIME"                    , columnDefinition="nvarchar(18) ") 
	private String deliveryTime;
	@Column(name = "STATUS_CODE"                      , columnDefinition="nvarchar(10) ") 
	private String statusCode;
	@Column(name = "STATUS_INFO"                      , columnDefinition="nvarchar(30) ") 
	private String statusInfo;
	@Column(name = "STATUS_SUCCESS"                   , columnDefinition="nvarchar(1)  ") 
	private String statusSuccess;
	@Column(name = "CREATE_DATE"                      , columnDefinition="nvarchar(8)  ") 
	private String createDate;
	@Column(name = "UPDATE_DATE"                      , columnDefinition="nvarchar(8)  ") 
	private String updateDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public String getFrontendSystem() {
		return frontendSystem;
	}
	public void setFrontendSystem(String frontendSystem) {
		this.frontendSystem = frontendSystem;
	}
	public String getDeliveryPathway() {
		return deliveryPathway;
	}
	public void setDeliveryPathway(String deliveryPathway) {
		this.deliveryPathway = deliveryPathway;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getAccountPccCode() {
		return accountPccCode;
	}
	public void setAccountPccCode(String accountPccCode) {
		this.accountPccCode = accountPccCode;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getMessageTemplate() {
		return messageTemplate;
	}
	public void setMessageTemplate(String messageTemplate) {
		this.messageTemplate = messageTemplate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getMessagePoint() {
		return messagePoint;
	}
	public void setMessagePoint(int messagePoint) {
		this.messagePoint = messagePoint;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getProgrCd() {
		return progrCd;
	}
	public void setProgrCd(String progrCd) {
		this.progrCd = progrCd;
	}
	public String getSegCd() {
		return segCd;
	}
	public void setSegCd(String segCd) {
		this.segCd = segCd;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCustomerCellPhoneNumber() {
		return customerCellPhoneNumber;
	}
	public void setCustomerCellPhoneNumber(String customerCellPhoneNumber) {
		this.customerCellPhoneNumber = customerCellPhoneNumber;
	}
	public String getLineBcUUID() {
		return lineBcUUID;
	}
	public void setLineBcUUID(String lineBcUUID) {
		this.lineBcUUID = lineBcUUID;
	}
	public String getScheduleDate() {
		return scheduleDate;
	}
	public void setScheduleDate(String scheduleDate) {
		this.scheduleDate = scheduleDate;
	}
	public String getScheduleTime() {
		return scheduleTime;
	}
	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	public String getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}
	public String getDeliveryTime() {
		return deliveryTime;
	}
	public void setDeliveryTime(String deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusInfo() {
		return statusInfo;
	}
	public void setStatusInfo(String statusInfo) {
		this.statusInfo = statusInfo;
	}
	public String getStatusSuccess() {
		return statusSuccess;
	}
	public void setStatusSuccess(String statusSuccess) {
		this.statusSuccess = statusSuccess;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	
	
//	@PrePersist
//	public void prePersist() {
//		createTime = Calendar.getInstance().getTime();
//		modifyTime = createTime;
//	}
//	
//	@PreUpdate
//	public void preUpdate() {
//		modifyTime = Calendar.getInstance().getTime();
//	}

    
	
}
