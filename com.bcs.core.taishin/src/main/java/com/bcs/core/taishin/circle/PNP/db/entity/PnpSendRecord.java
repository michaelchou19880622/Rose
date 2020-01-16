package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "BCS_PNP_SEND_RECORD",
        indexes = {
                @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID"),
                @Index(name = "INDEX_1", columnList = "PHONE"),
        })
public class PnpSendRecord extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PNP_SEND_ID")
    private Long pnpSendId;

    @Column(name = "PNP_MAIN_ID")
    private Long pnpMainId;

    @Column(name = "PNP_DETAIL_ID")
    private Long pnpDetailId;

    @Column(name = "SEND_TIME")
    private Date sendTime;

    @Column(name = "PHONE", columnDefinition = "nvarchar(15)")
    private String phone;

    @Column(name = "CHANNEL_ID", columnDefinition = "nvarchar(50)")
    private String channelId;

    @Column(name = "API_TYPE", columnDefinition = "nvarchar(50)")
    private String apiType;

    @Column(name = "RECORD", columnDefinition = "nvarchar(1000)")
    private String record;

    public Long getPnpSendId() {
        return pnpSendId;
    }

    public void setPnpSendId(Long pnpSendId) {
        this.pnpSendId = pnpSendId;
    }

    public Long getPnpMainId() {
        return pnpMainId;
    }

    public void setPnpMainId(Long pnpMainId) {
        this.pnpMainId = pnpMainId;
    }

    public Long getPnpDetailId() {
        return pnpDetailId;
    }

    public void setPnpDetailId(Long pnpDetailId) {
        this.pnpDetailId = pnpDetailId;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
        if (this.record.length() > 1000) {
            this.record = this.record.substring(0, 999);
        }
    }
}
