package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_ESN_DETAIL", indexes = {
		@Index(name = "INDEX_0", columnList = "ESN_ID,ESN"),
		@Index(name = "INDEX_1", columnList = "ESN_ID"),
		@Index(name = "INDEX_2", columnList = "ESN_ID,UID,STATUS"),
		@Index(name = "INDEX_2", columnList = "ESN_ID,STATUS")
	})
public class LineUserBinded extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String STATUS_READY = "READY";
	public static final String STATUS_FINISH = "FINISH";
	public static final String STATUS_FAIL = "FAIL";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ESN_DETAIL_ID")
	private Long esnDetailId;
	
	@Column(name = "ESN_ID", columnDefinition="nvarchar(50)")
	private String esnId;
	
	@Column(name = "ESN", columnDefinition="nvarchar(1000)")
	private String esn;
	
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String uid;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "SEND_TIME")
    private Date sendTime;

    public Long getEsnDetailId() {
        return esnDetailId;
    }

    public void setEsnDetailId(Long esnDetailId) {
        this.esnDetailId = esnDetailId;
    }

    public String getEsnId() {
        return esnId;
    }

    public void setEsnId(String esnId) {
        this.esnId = esnId;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

}
