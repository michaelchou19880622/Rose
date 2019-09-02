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
 *  三竹  body
 * 欄位    型態 長度 説明 備註
 * DestCategory    Char      8  ”掛帳代碼” =>  PCC Code       此欄位新版簡訊無特別用處                       Dept_id(必填)

 * DestName        Varchar   20 請填入系統有意義之流水號(open端可辯示之唯一序號)                   Msg_idx

 * DestNo          Varchar   20 手機門號/請填入09帶頭的手機號碼。                                                                                          必填 Tel(必填)

 * MsgData         Varchar      請勿輸入 % $ '  字元，不可使用‘&’分隔號，或以全型字使用/簡訊內容。
 *                              若有換行的需求，請以ASCII Code 6代表換行。必填。(333個字)         Content(必填)
 * 
 * @author Kenneth
 *
 */

@Entity
@Table(name = "BCS_PNP_DETAIL_MITAKE",
indexes = {
	       @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID"),
	       @Index(name = "INDEX_1", columnList = "STATUS"),
	       @Index(name = "INDEX_2", columnList = "PROC_STAGE"),	       
	       @Index(name = "INDEX_3", columnList = "PNP_DELIVERY_EXPIRE_TIME"),
	})
public class PnpDetailMitake extends PnpDetail {
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
	
	//對應原生欄位DestNo
	@Column(name = "PHONE", columnDefinition="nvarchar(15)")
	private String phone;
	//sha256 e.164 hash for line pnp push
	@Column(name = "PHONE_HASH", columnDefinition="nvarchar(100)")
    private String phoneHash;

	//對應原生欄位 MsgData
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
	
	//來源資料原生欄位
	@Column(name = "DEST_CATEGORY" ,columnDefinition="nvarchar(15)")
	private String DestCategory;
	@Column(name = "DEST_NAME" ,columnDefinition="nvarchar(36)")
	private String DestName;
	
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

	public String getDestName() {
		return DestName;
	}

	public void setDestName(String destName) {
		DestName = destName;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getDestCategory() {
		return DestCategory;
	}

	public void setDestCategory(String destCategory) {
		DestCategory = destCategory;
	}
	
}
