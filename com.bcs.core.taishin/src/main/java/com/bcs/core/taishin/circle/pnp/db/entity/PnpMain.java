package com.bcs.core.taishin.circle.pnp.db.entity;

import com.bcs.core.json.AbstractBcsEntity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 與FTP的來源檔案為1對1關係
 *
 * @author ???
 */
@MappedSuperclass
public class PnpMain extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PNP_MAIN_ID")
    private Long pnpMainId;

    /**
     * 資料來源//三竹=1;互動來源=2;明宣=3;unica=4
     */
    @Column(name = "SOURCE", columnDefinition = "nvarchar(3)")
    private String source;

    /**
     * 通路參數
     */
    @Column(name = "PROC_FLOW", columnDefinition = "nvarchar(3)")
    private String procFlow;

    /**
     * 主檔通路執行階段；select for update wait時更新此欄位，防止重複發送
     * 對應procFlow的各種階段；以procFlow=3(PROC_FLOW_BC_PNP_SMS)為例，則分為BC、PNP、SMS三個procStage，stage之中分各種status
     */
    @Column(name = "PROC_STAGE", columnDefinition = "nvarchar(50)")
    private String procStage;


    /**
     * 主檔狀態；select for update wait時更新此欄位，防止重複發送
     */
    @Column(name = "STATUS", columnDefinition = "nvarchar(50)")
    private String status;

    /**
     * Status
     */
    @Column(name = "STATUS_NOTICE", columnDefinition = "nvarchar(200)")
    private String statusNotice;

    /**
     * 樣版ID
     */
    @Column(name = "TEMP_ID", columnDefinition = "nvarchar(50)")
    private String tempId;

    /**
     * 預留欄位分產品用
     */
    @Column(name = "GROUP_ID")
    private Long groupId;

    /**
     * DELAY :預約發送；IMMEDIATE：立即發送
     */
    @Column(name = "SEND_TYPE", columnDefinition = "nvarchar(20)")
    private String sendType;

    /**
     * 總數量
     */
    @Column(name = "TOTAL_COUNT")
    private Long totalCount;

    /**
     * 發送數量
     */
    @Column(name = "SEND_COUNT")
    private Long sendCount;

    /**
     * 發送時間
     */
    @Column(name = "SEND_TIME")
    private Date sendTime;

    /**
     * Line發送時間
     */
    @Column(name = "LINE_PUSH_TIME")
    private Date linePushTime;

    /**
     * PNP發送時間
     */
    @Column(name = "PNP_TIME")
    private Date pnpTime;

    /**
     * 簡訊發送時間
     */
    @Column(name = "SMS_TIME")
    private Date smsTime;

    /**
     * DELAY :預約發送時才會有；IMMEDIATE：立即發送 = null
     */
    @Column(name = "SCHEDULE_TIME", columnDefinition = "nvarchar(50)")
    private String scheduleTime;

    /**
     * 建立時間
     */
    @Column(name = "CREAT_TIME")
    private Date createTime;

    /**
     * 取檔發送AP的hostName，select for update wait時更新此欄位，防止重複發送
     */
    @Column(name = "PROC_AP_NAME", columnDefinition = "nvarchar(50)")
    private String procApName;

    /**
     * FTP來源檔原名
     */
    @Column(name = "ORIG_FILE_NAME", columnDefinition = "nvarchar(200)")
    private String origFileName;

    /**
     * 更新時間
     */
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 不會異動ＤＢ 物件傳遞暫存用，資料會存在Detail中
     */
    @Transient
    private String smsFileName;

    /**
     * 不會異動ＤＢ 物件傳遞暫存用
     */
    @Transient
    private List<? super PnpDetail> pnpDetails;

    /**
     * 不會異動ＤＢ 物件傳遞暫存用
     */
    @Transient
    private PnpContentTemplateMsg template;

    /**
     * 不會異動ＤＢ 物件傳遞暫存用
     */
    @Transient
    private List<PnpContentTemplateMsgAction> templateActions;

    @PrePersist
    public void prePersist() {
        createTime = Calendar.getInstance().getTime();
        modifyTime = createTime;
    }

    @PreUpdate
    public void preUpdate() {
        modifyTime = Calendar.getInstance().getTime();
    }

    public List<? super PnpDetail> getPnpDetails() {
        return pnpDetails;
    }

    public void setPnpDetails(List<? super PnpDetail> details) {
        this.pnpDetails = details;
    }

    public Long getPnpMainId() {
        return pnpMainId;
    }

    public void setPnpMainId(Long pnpMainId) {
        this.pnpMainId = pnpMainId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusNotice() {
        return statusNotice;
    }

    public void setStatusNotice(String statusNotice) {
        this.statusNotice = statusNotice;
        if (this.statusNotice.length() > 200) {
            this.statusNotice = this.statusNotice.substring(0, 199);
        }
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSendCount() {
        return sendCount;
    }

    public void setSendCount(Long sendCount) {
        this.sendCount = sendCount;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getProcApName() {
        return procApName;
    }

    public void setProcApName(String procApName) {
        this.procApName = procApName;
    }

    public String getProcFlow() {
        return procFlow;
    }


    public void setProcFlow(String procFlow) {
        this.procFlow = procFlow;
    }


    public String getProcStage() {
        return procStage;
    }


    public void setProcStage(String procStage) {
        this.procStage = procStage;
    }


    public Date getLinePushTime() {
        return linePushTime;
    }


    public void setLinePushTime(Date linePushTime) {
        this.linePushTime = linePushTime;
    }


    public Date getPnpTime() {
        return pnpTime;
    }


    public void setPnpTime(Date pnpTime) {
        this.pnpTime = pnpTime;
    }


    public Date getSmsTime() {
        return smsTime;
    }


    public void setSmsTime(Date smsTime) {
        this.smsTime = smsTime;
    }


    public String getOrigFileName() {
        return origFileName;
    }

    public void setOrigFileName(String origFileName) {
        this.origFileName = origFileName;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public PnpContentTemplateMsg getTemplate() {
        return template;
    }


    public void setTemplate(PnpContentTemplateMsg template) {
        this.template = template;
    }


    public List<PnpContentTemplateMsgAction> getTemplateActions() {
        return templateActions;
    }


    public void setTemplateActions(List<PnpContentTemplateMsgAction> templateActions) {
        this.templateActions = templateActions;
    }


    public String getSmsFileName() {
        return smsFileName;
    }


    public void setSmsFileName(String smsFileName) {
        this.smsFileName = smsFileName;
    }

}
