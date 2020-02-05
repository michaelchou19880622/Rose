package com.bcs.core.taishin.circle.pnp.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;


/**
 * PNP 訊息明細物件
 *
 * @author Kenneth
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
    private static final long serialVersionUid = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "RECORD_ID", columnDefinition = "nvarchar(128)")
    private String recordId;
    @Column(name = "FRONTEND_SYSTEM", columnDefinition = "nvarchar(30)")
    private String frontendSystem;
    @Column(name = "DELIVERY_PATHWAY", columnDefinition = "nvarchar(200)")
    private String deliveryPathway;
    @Column(name = "CHANNEL", columnDefinition = "nvarchar(30)")
    private String channel;
    @Column(name = "ACCOUNT", columnDefinition = "nvarchar(30)")
    private String account;
    @Column(name = "ACCOUNT_PCCCODE", columnDefinition = "nvarchar(10)")
    private String accountPccCode;
    @Column(name = "BATCH_NO", columnDefinition = "nvarchar(64)")
    private String batchNo;
    @Column(name = "SERIAL_NO", columnDefinition = "nvarchar(64)")
    private String serialNo;
    @Column(name = "MESSAGE_TEMPLATE", columnDefinition = "nvarchar(30)")
    private String messageTemplate;
    @Column(name = "MESSAGE", columnDefinition = "nvarchar(700)")
    private String message;
    @Column(name = "MESSAGE_POINT", columnDefinition = "nvarchar(10)")
    private int messagePoint;
    @Column(name = "CAMPAIGN_ID", columnDefinition = "nvarchar(30)")
    private String campaignId;
    @Column(name = "PROGR_CD", columnDefinition = "nvarchar(20)")
    private String progrCd;
    @Column(name = "SEG_CD", columnDefinition = "nvarchar(10)")
    private String segCd;
    @Column(name = "CUSTOMER_ID", columnDefinition = "nvarchar(64)")
    private String customerId;
    @Column(name = "CUSTOMER_CellPhone_Number", columnDefinition = "nvarchar(64)")
    private String customerCellPhoneNumber;
    @Column(name = "Line_BC_UID", columnDefinition = "nvarchar(50)")
    private String lineBcUid;
    @Column(name = "SCHEDULE_DATE", columnDefinition = "nvarchar(18)")
    private String scheduleDate;
    @Column(name = "SCHEDULE_TIME", columnDefinition = "nvarchar(18)")
    private String scheduleTime;
    @Column(name = "DELIVERY_DATE", columnDefinition = "nvarchar(18)")
    private String deliveryDate;
    @Column(name = "DELIVERY_TIME", columnDefinition = "nvarchar(18)")
    private String deliveryTime;

    //    -------------------FIXME 待DB新建欄位後開放---------------------------------
    /**
     * 發送BC Line回傳HttpStatusCode
     */
//    @Column(name = "BC_STATUS_CODE", columnDefinition = "nvarchar(10)")
//    private String bcStatusCode;

    /**
     * 發送PNP Line回傳HttpStatusCode
     */
//    @Column(name = "PNP_STATUS_CODE", columnDefinition = "nvarchar(10)")
//    private String pnpStatusCode;
    //    -------------------FIXME 待DB新建欄位後開放---------------------------------

    //    -------------------FIXME 待DB欄位更名為PNP_STATUS_CODE後移除----------------
    @Deprecated
    @Column(name = "STATUS_CODE", columnDefinition = "nvarchar(10)")
    private String statusCode;
    //    -------------------FIXME 待DB欄位更名為PNP_STATUS_CODE後移除----------------


    @Column(name = "STATUS_INFO", columnDefinition = "nvarchar(30)")
    private String statusInfo;
    @Column(name = "STATUS_SUCCESS", columnDefinition = "nvarchar(1)")
    private String statusSuccess;
    @Column(name = "CREATE_DATE", columnDefinition = "nvarchar(8)")
    private String createDate;
    @Column(name = "UPDATE_DATE", columnDefinition = "nvarchar(8)")
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

    public String getLineBcUid() {
        return lineBcUid;
    }

    public void setLineBcUid(String lineBcUid) {
        this.lineBcUid = lineBcUid;
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

//    -------------------FIXME 待DB新建欄位後開放---------------------------------
//    public String getBcStatusCode() {
//        return bcStatusCode;
//    }
//
//    public void setBcStatusCode(String bcStatusCode) {
//        this.bcStatusCode = bcStatusCode;
//    }
//
//    public String getPnpStatusCode() {
//        return pnpStatusCode;
//    }
//
//    public void setPnpStatusCode(String pnpStatusCode) {
//        this.pnpStatusCode = pnpStatusCode;
//    }
//    --------------------------------------------------------------------------


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
