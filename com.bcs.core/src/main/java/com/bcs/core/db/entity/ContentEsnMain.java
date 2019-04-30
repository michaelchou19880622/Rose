package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_ESN_MAIN", indexes = {
		@Index(name = "INDEX_0", columnList = "STATUS")
	})
public class ContentEsnMain extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DISABLE = "DISABLE";
	public static final String STATUS_DELETE = "DELETE";
	
	public static final String SEND_STATUS_READY = "READY";
    public static final String SEND_STATUS_PROCESS = "PROCESS";
    public static final String SEND_STATUS_FINISH = "FINISH";

	@Id
	@Column(name = "ESN_ID", columnDefinition="nvarchar(50)")
	private String esnId;

	@Column(name = "ESN_NAME", columnDefinition="nvarchar(50)")
	private String esnName;
	
	@Column(name = "ESN_MSG", columnDefinition="nvarchar(500)")
	private String esnMsg;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "SEND_STATUS", columnDefinition="nvarchar(50)")
    private String sendStatus = SEND_STATUS_READY;

    public String getEsnId() {
        return esnId;
    }

    public void setEsnId(String esnId) {
        this.esnId = esnId;
    }

    public String getEsnName() {
        return esnName;
    }

    public void setEsnName(String esnName) {
        this.esnName = esnName;
    }

    public String getEsnMsg() {
        return esnMsg;
    }

    public void setEsnMsg(String esnMsg) {
        this.esnMsg = esnMsg;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

}
