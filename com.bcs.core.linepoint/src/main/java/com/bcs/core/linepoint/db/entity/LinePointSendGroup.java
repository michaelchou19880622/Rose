package com.bcs.core.linepoint.db.entity;

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
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "BCS_LINE_POINT_SEND_GROUP")
public class LinePointSendGroup extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	public static final String GROUP_TYPE_DEFAULT = "DEFAULT";
	public static final String GROUP_TYPE_NORMAL = "NORMAL";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "GROUP_ID")
	private Long groupId;
	
	@Column(name = "GROUP_TYPE", columnDefinition="nvarchar(20)")
	private String groupType;
	
	@Column(name = "GROUP_TITLE", columnDefinition="nvarchar(50)")
	private String groupTitle;

	@Column(name = "LINE_POINT_GROUP_ID")
	private Long richMenuGroupId;
	
	@Column(name = "LINE_POINT_GROUP_NAME", columnDefinition="nvarchar(50)")
	private String richMenuGroupName;
	
	@Column(name = "GROUP_DESCRIPTION", columnDefinition="nvarchar(1000)")
	private String groupDescription;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "LINE_POINT_START_USING_TIME")
	private Date richMenuStartUsingTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "LINE_POINT_END_USING_TIME")
	private Date richMenuEndUsingTime;
	
	@Column(name = "USE_START_TIME_SCHEDULER")
	private Boolean useStartTimeScheduler;

	@Column(name = "USE_END_TIME_SCHEDULER")
	private Boolean useEndTimeScheduler;
	
	@OneToMany(mappedBy="sendGroup",cascade=CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval=true)
	private List<LinePointSendGroupDetail> sendGroupDetail;

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

	public List<LinePointSendGroupDetail> getSendGroupDetail() {
		return sendGroupDetail;
	}

	public void setSendGroupDetail(List<LinePointSendGroupDetail> sendGroupDetail) {
		this.sendGroupDetail = sendGroupDetail;
	}

	public Long getRichMenuGroupId() {
		return richMenuGroupId;
	}

	public void setRichMenuGroupId(Long richMenuGroupId) {
		this.richMenuGroupId = richMenuGroupId;
	}

	public String getRichMenuGroupName() {
		return richMenuGroupName;
	}

	public void setRichMenuGroupName(String richMenuGroupName) {
		this.richMenuGroupName = richMenuGroupName;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public Date getRichMenuStartUsingTime() {
		return richMenuStartUsingTime;
	}

	public void setRichMenuStartUsingTime(Date richMenuStartUsingTime) {
		this.richMenuStartUsingTime = richMenuStartUsingTime;
	}

	public Date getRichMenuEndUsingTime() {
		return richMenuEndUsingTime;
	}

	public void setRichMenuEndUsingTime(Date richMenuEndUsingTime) {
		this.richMenuEndUsingTime = richMenuEndUsingTime;
	}

	public Boolean getUseStartTimeScheduler() {
		return useStartTimeScheduler;
	}

	public void setUseStartTimeScheduler(Boolean useStartTimeScheduler) {
		this.useStartTimeScheduler = useStartTimeScheduler;
	}

	public Boolean getUseEndTimeScheduler() {
		return useEndTimeScheduler;
	}

	public void setUseEndTimeScheduler(Boolean useEndTimeScheduler) {
		this.useEndTimeScheduler = useEndTimeScheduler;
	}
}
