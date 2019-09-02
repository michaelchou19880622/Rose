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

import com.bcs.core.json.AbstractBcsEntity;


/**
 * 
 * 分隔符號       ;;   char	  1	   N	 分隔符號
 * 
 * 明宣 body
 *             分隔符號為兩個分號";;"
 *             流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
 * 
 * @author Kenneth
 *
 */

@Entity
@Table(name = "BCS_PNP_DETAIL_MING",
indexes = {
	       @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID"),
	       @Index(name = "INDEX_1", columnList = "STATUS"),
	       @Index(name = "INDEX_2", columnList = "PROC_STAGE"),	       
	       @Index(name = "INDEX_3", columnList = "PNP_DELIVERY_EXPIRE_TIME"),
	})
public class PnpDetailMing extends PnpDetail {
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
	
	//對應原生欄位"手機號碼"
	@Column(name = "PHONE", columnDefinition="nvarchar(15)")
	private String phone;
	//sha256 e.164 hash for line pnp push
	@Column(name = "PHONE_HASH", columnDefinition="nvarchar(100)")
    private String phoneHash;

	//對應原生欄位 "簡訊內容"
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
	//流水號
	@Column(name = "SN" ,columnDefinition="nvarchar(15)")
	private String SN;
	//預約時間
	@Column(name = "DETAIL_SCHEDULE_TIME" ,columnDefinition="nvarchar(20)")
	private String detailScheduleTime;
	//批次帳號1
	@Column(name = "ACCOUNT1" ,columnDefinition="nvarchar(20)")
	private String account1;
	//批次帳號2
	@Column(name = "ACCOUNT2" ,columnDefinition="nvarchar(20)")
	private String account2;
	//保留欄位1
	@Column(name = "VARIABLE1" ,columnDefinition="nvarchar(15)")
	private String Variable1;
	//保留欄位2
	@Column(name = "VARIABLE2" ,columnDefinition="nvarchar(15)")
	private String Variable2;
	//有效秒數
	@Column(name = "KEEP_SECOND" ,columnDefinition="nvarchar(15)")
	private String keepSecond;
	
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

	public String getSN() {
		return SN;
	}

	public void setSN(String sN) {
		SN = sN;
	}

	public String getVariable1() {
		return Variable1;
	}

	public void setVariable1(String variable1) {
		Variable1 = variable1;
	}

	public String getVariable2() {
		return Variable2;
	}

	public void setVariable2(String variable2) {
		Variable2 = variable2;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getDetailScheduleTime() {
		return detailScheduleTime;
	}

	public void setDetailScheduleTime(String detailScheduleTime) {
		this.detailScheduleTime = detailScheduleTime;
	}

	public String getAccount1() {
		return account1;
	}

	public void setAccount1(String account1) {
		this.account1 = account1;
	}

	public String getAccount2() {
		return account2;
	}

	public void setAccount2(String account2) {
		this.account2 = account2;
	}

	public String getKeepSecond() {
		return keepSecond;
	}

	public void setKeepSecond(String keepSecond) {
		this.keepSecond = keepSecond;
	}
	
}
