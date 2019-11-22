package com.bcs.core.taishin.circle.PNP.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 互動 header
 * &          char        1       N        分隔符號
 * 名稱	                         屬性                         長度                 Null?     說明
 * Subject	    NVARCHAR   200      Y        簡訊主旨
 * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
 * Password     CHAR                Y        使用者密碼(可不填)
 * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
 * ExprieTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
 * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
 * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
 *
 * @author Kenneth
 */

@Entity
@Table(name = "BCS_PNP_MAIN_EVERY8D",
        indexes = {
                @Index(name = "INDEX_0", columnList = "SEND_TYPE"),
                @Index(name = "INDEX_1", columnList = "STATUS"),
                @Index(name = "INDEX_2", columnList = "PROC_FLOW"),
                @Index(name = "INDEX_3", columnList = "SOURCE"),
                @Index(name = "INDEX_4", columnList = "PROC_STAGE"),
        })
//與FTP的來源檔案為1對1關係
public class PnpMainEvery8d extends PnpMain {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PNP_MAIN_ID")
    private Long pnpMainId;

    //資料來源//互動來源=2
    @Column(name = "SOURCE", columnDefinition = "nvarchar(3)")
    private String source;

    //通路參數
    @Column(name = "PROC_FLOW", columnDefinition = "nvarchar(3)")
    private String procFlow;

    /**
     * 主檔通路執行階段；select for update wait時更新此欄位，防止重複發送
     * 對應procFlow的各種階段；以procFlow=3(PROC_FLOW_BC_PNP_SMS)為例，則分為BC、PNP、SMS三個procStage，stage之中分各種status
     */
    @Column(name = "PROC_STAGE", columnDefinition = "nvarchar(50)")
    private String procStage;


    //主檔狀態；select for update wait時更新此欄位，防止重複發送
    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status;

    @Column(name = "STATUS_NOTICE", columnDefinition = "nvarchar(200)")
    private String statusNotice;

    //樣版ID
    @Column(name = "TEMP_ID", columnDefinition = "nvarchar(50)")
    private String tempId;

    //預留欄位分產品用
    @Column(name = "GROUP_ID")
    private Long groupId;

    //DELAY :預約發送；IMMEDIATE：立即發送
    @Column(name = "SEND_TYPE", columnDefinition = "nvarchar(10)")
    private String sendType;

    @Column(name = "TOTAL_COUNT")
    private Long totalCount;

    @Column(name = "SEND_COUNT")
    private Long sendCount;

    @Column(name = "LINE_PUSH_TIME")
    private Date linePushTime;

    @Column(name = "PNP_TIME")
    private Date pnpTime;

    @Column(name = "SMS_TIME")
    private Date smsTime;

    //DELAY :預約發送時才會有；IMMEDIATE：立即發送 = null
    @Column(name = "SCHEDULE_TIME", columnDefinition = "nvarchar(50)")
    private String scheduleTime;

    @Column(name = "CREAT_TIME")
    private Date createTime;

    //取檔發送AP的hostName，select for update wait時更新此欄位，防止重複發送
    @Column(name = "PROC_AP_NAME", columnDefinition = "nvarchar(50)")
    private String procApName;

    //FTP來源檔原名
    @Column(name = "ORIG_FILE_NAME", columnDefinition = "nvarchar(200)")
    private String origFileName;

    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    @Column(name = "PNP_MAINTAIN_ACCOUNT_ID")
    private Long pnpMaintainAccountId;

    //來源資料原生欄位
    @Column(name = "SUBJECT", columnDefinition = "nvarchar(200)")
    private String Subject;
    @Column(name = "USER_ID", columnDefinition = "nvarchar(20)")
    private String UserID;
    @Column(name = "PASSWORD", columnDefinition = "nvarchar(20)")
    private String Password;
    @Column(name = "ORDER_TIME", columnDefinition = "nvarchar(20)")
    private String OrderTime;
    @Column(name = "EXPRIE_TIME", columnDefinition = "nvarchar(20)")
    private String ExprieTime;
    @Column(name = "MSG_TYPE", columnDefinition = "nvarchar(1)")
    private String MsgType;
    @Column(name = "BATCH_ID", columnDefinition = "nvarchar(36)")
    private String BatchID;

    /**
     * 不會異動ＤＢ 物件傳遞暫存用
     */
    @Transient
    private List<PnpDetailEvery8d> pnpDetailEvery8dList;

    @Override
    @PrePersist
    public void prePersist() {
        createTime = Calendar.getInstance().getTime();
        modifyTime = createTime;
    }

    @Override
    @PreUpdate
    public void preUpdate() {
        modifyTime = Calendar.getInstance().getTime();
    }

    @Override
    public Long getPnpMainId() {
        return pnpMainId;
    }

    @Override
    public void setPnpMainId(Long pnpMainId) {
        this.pnpMainId = pnpMainId;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getStatusNotice() {
        return statusNotice;
    }

    @Override
    public void setStatusNotice(String statusNotice) {
        this.statusNotice = statusNotice;
        if (this.statusNotice.length() > 200) {
            this.statusNotice = this.statusNotice.substring(0, 199);
        }
    }

    @Override
    public Long getTotalCount() {
        return totalCount;
    }

    @Override
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public Long getSendCount() {
        return sendCount;
    }

    @Override
    public void setSendCount(Long sendCount) {
        this.sendCount = sendCount;
    }

    @Override
    public String getSendType() {
        return sendType;
    }

    @Override
    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    @Override
    public String getScheduleTime() {
        return scheduleTime;
    }

    @Override
    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    @Override
    public String getTempId() {
        return tempId;
    }

    @Override
    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    @Override
    public Long getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String getProcApName() {
        return procApName;
    }

    @Override
    public void setProcApName(String procApName) {
        this.procApName = procApName;
    }

    @Override
    public String getOrigFileName() {
        return origFileName;
    }

    @Override
    public void setOrigFileName(String origFileName) {
        this.origFileName = origFileName;
    }

    @Override
    public Date getModifyTime() {
        return modifyTime;
    }

    @Override
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getOrderTime() {
        return OrderTime;
    }

    public void setOrderTime(String orderTime) {
        OrderTime = orderTime;
    }

    public String getExprieTime() {
        return ExprieTime;
    }

    public void setExprieTime(String exprieTime) {
        ExprieTime = exprieTime;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }

    public String getBatchID() {
        return BatchID;
    }

    public void setBatchID(String batchID) {
        BatchID = batchID;
    }

    @Override
    public String getProcFlow() {
        return procFlow;
    }

    @Override
    public void setProcFlow(String procFlow) {
        this.procFlow = procFlow;
    }

    @Override
    public String getProcStage() {
        return procStage;
    }

    @Override
    public void setProcStage(String procStage) {
        this.procStage = procStage;
    }

    @Override
    public Date getLinePushTime() {
        return linePushTime;
    }

    @Override
    public void setLinePushTime(Date linePushTime) {
        this.linePushTime = linePushTime;
    }

    @Override
    public Date getPnpTime() {
        return pnpTime;
    }

    @Override
    public void setPnpTime(Date pnpTime) {
        this.pnpTime = pnpTime;
    }

    @Override
    public Date getSmsTime() {
        return smsTime;
    }

    @Override
    public void setSmsTime(Date smsTime) {
        this.smsTime = smsTime;
    }

    public Long getPnpMaintainAccountId() {
        return pnpMaintainAccountId;
    }

    public void setPnpMaintainAccountId(Long pnpMaintainAccountId) {
        this.pnpMaintainAccountId = pnpMaintainAccountId;
    }

    public List<PnpDetailEvery8d> getPnpDetailEvery8dList() {
        return pnpDetailEvery8dList;
    }

    public void setPnpDetailEvery8dList(List<PnpDetailEvery8d> pnpDetailEvery8dList) {
        this.pnpDetailEvery8dList = pnpDetailEvery8dList;
    }
}
