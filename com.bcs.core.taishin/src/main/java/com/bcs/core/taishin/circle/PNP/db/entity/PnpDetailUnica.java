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
 * Uinca 都是互動格式  【台新 Line PNP】相關問題
 * From: 洪志豪 <shaunhung@taishinbank.com.tw> 
 * Sent: Friday, April 19, 2019 10:33 AM
 * 
 * 
 * 分隔符號       &	   char	  1	   N	 分隔符號
 * 
 * 互動 body
 *   名稱	                          屬性	                       長度	  Null?	          說明
 *   SN	        char        15     N     名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
 *   DestName   char        36	   Y     收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
 *   Mobile	    char        20	   N     收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
 *   Content    nvarchar    756	   N     簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
 *   PID        char        11	   Y     身份字號
 *   CampaignID	varchar	    28     Y     行銷活動代碼(可為空值)
 *   SegmentID	varchar	    10     Y     客群代號(可為空值)
 *   ProgramID	varchar	    20     Y     階段代號(可為空值)
 *   Variable1	varchar	    15     Y     擴充欄位1(可為空值)
 *   Variable2	varchar	    15     Y     擴充欄位2(可為空值)
 * 
 * @author Kenneth
 *
 */

@Entity
@Table(name = "BCS_PNP_DETAIL_UNICA",
indexes = {
	       @Index(name = "INDEX_0", columnList = "PNP_MAIN_ID")
	})
public class PnpDetailUnica extends PnpDetail {
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
	
	//來源資料原生欄位
	@Column(name = "SN" ,columnDefinition="nvarchar(15)")
	private String SN;
	@Column(name = "DEST_NAME" ,columnDefinition="nvarchar(36)")
	private String DestName;
//	private String Mobile;
//	private String Content;
	@Column(name = "PID" ,columnDefinition="nvarchar(11)")
	private String PID;
	@Column(name = "CAMPAIGN_ID" ,columnDefinition="nvarchar(28)")
	private String CampaignID;
	@Column(name = "SEGMENT_ID" ,columnDefinition="nvarchar(10)")
	private String SegmentID;
	@Column(name = "PROGRAM_ID" ,columnDefinition="nvarchar(20)")
	private String ProgramID;
	@Column(name = "VARIABLE1" ,columnDefinition="nvarchar(15)")
	private String Variable1;
	@Column(name = "VARIABLE2" ,columnDefinition="nvarchar(15)")
	private String Variable2;
	
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

	public String getDestName() {
		return DestName;
	}

	public void setDestName(String destName) {
		DestName = destName;
	}

	public String getPID() {
		return PID;
	}

	public void setPID(String pID) {
		PID = pID;
	}

	public String getCampaignID() {
		return CampaignID;
	}

	public void setCampaignID(String campaignID) {
		CampaignID = campaignID;
	}

	public String getSegmentID() {
		return SegmentID;
	}

	public void setSegmentID(String segmentID) {
		SegmentID = segmentID;
	}

	public String getProgramID() {
		return ProgramID;
	}

	public void setProgramID(String programID) {
		ProgramID = programID;
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
	
}
