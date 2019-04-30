package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_REWARD_CARD_POINT")
public class ContentRewardCardPoint extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String STATUS_DELETE = "DELETE";
	public static final String STATUS_ACTIVE = "ACTIVE";
	
	public ContentRewardCardPoint() {
		super();
	}
	
	@Id
	@Column(name = "REWARDCARD_POINT_ID", columnDefinition="nvarchar(50)")
	private String rewardCardPointId;
	
	@Column(name = "REWARDCARD_ID")
	private String rewardCardId;
	
	@Column(name = "STATUS", columnDefinition = "nvarchar(20)")
	private String status = STATUS_ACTIVE;

	@Column(name = "QRCODE_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String qrcodeImageId;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "MODIFY_USER", columnDefinition = "nvarchar(50)")
	private String modifyUser;

	public String getRewardCardPointId() {
		return rewardCardPointId;
	}

	public void setRewardCardPointId(String rewardCardPointId) {
		this.rewardCardPointId = rewardCardPointId;
	}

	public String getRewardCardId() {
		return rewardCardId;
	}

	public void setRewardCardId(String rewardCardId) {
		this.rewardCardId = rewardCardId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getQrcodeImageId() {
		return qrcodeImageId;
	}

	public void setQrcodeImageId(String qrcodeImageId) {
		this.qrcodeImageId = qrcodeImageId;
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

}
