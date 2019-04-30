package com.bcs.core.db.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_SEND_GROUP")
public class SendGroup extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "GROUP_ID")
	private Long groupId;

	@Column(name = "GROUP_TITLE", columnDefinition="nvarchar(50)")
	private String groupTitle;

	@Column(name = "GROUP_DESCRIPTION", columnDefinition="nvarchar(1000)")
	private String groupDescription;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@OneToMany(mappedBy="sendGroup",cascade=CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval=true)
	private List<SendGroupDetail> sendGroupDetail;

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public List<SendGroupDetail> getSendGroupDetail() {
		return sendGroupDetail;
	}

	public void setSendGroupDetail(List<SendGroupDetail> sendGroupDetail) {
		this.sendGroupDetail = sendGroupDetail;
	}
	
}
