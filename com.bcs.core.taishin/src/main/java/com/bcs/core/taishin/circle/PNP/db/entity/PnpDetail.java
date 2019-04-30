package com.bcs.core.taishin.circle.PNP.db.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.bcs.core.json.AbstractBcsEntity;

@MappedSuperclass
public class PnpDetail extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "PNP_DETAIL_ID")
	private Long pnpDetailId;

	@Column(name = "PNP_MAIN_ID")
	private Long pnpMainId;

	//Line uid ; push line 訊息時才會用phone回查lineUser取得
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String uid;
	
	//資料來源//三竹=1;互動來源=2;明宣=3;unica=4
	@Column(name = "SOURCE", columnDefinition="nvarchar(3)")
	private String source;
	
	//對應原生欄位Mobile
	@Column(name = "PHONE", columnDefinition="nvarchar(15)")
	private String phone;
	//sha256 e.164 hash for line pnp push
	@Column(name = "PHONE_HASH", columnDefinition="nvarchar(100)")
    private String phoneHash;

	//對應原生欄位 Content
	@Column(name = "MSG", columnDefinition="nvarchar(1000)")
	private String msg;

	@Column(name = "CREAT_TIME")
	private Date createTime;
	
	@Column(name = "SEND_TIME")
	private Date sendTime;
	
	//明細檔狀態；select for update wait時更新此欄位，防止重複發送
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	//通路參數 
	@Column(name = "PROC_FLOW", columnDefinition="nvarchar(3)")
	private String procFlow;
	
	/**
	 * 主檔通路執行階段；select for update wait時更新此欄位，防止重複發送
	 * 對應procFlow的各種階段；以procFlow=3(PROC_FLOW_BC_PNP_SMS)為例，則分為BC、PNP、SMS三個procStage，stage之中分各種status
	 */
	@Column(name = "PROC_STAGE", columnDefinition="nvarchar(50)")
	private String procStage;
	
	@Column(name = "LINE_PUSH_TIME")
	private Date linePushTime;
	
	@Column(name = "PNP_TIME")
	private Date pnpTime;
	
	@Column(name = "SMS_TIME")
	private Date smsTime;
	
	@PrePersist
	public void prePersist() {
		createTime = Calendar.getInstance().getTime();
		modifyTime = createTime;
	}
	
	@PreUpdate
	public void preUpdate() {
		modifyTime = Calendar.getInstance().getTime();
	}

    public Long getPnpDetailId() {
        return pnpDetailId;
    }

    public void setPnpDetailId(Long pnpDetailId) {
        this.pnpDetailId = pnpDetailId;
    }

    public Long getPnpMainId() {
        return pnpMainId;
    }

    public void setPnpMainId(Long pnpMainId) {
        this.pnpMainId = pnpMainId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneHash() {
        return phoneHash;
    }

    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}
