package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_RICH_DETAIL",
indexes = {
	       @Index(name = "INDEX_0", columnList = "RICH_ID"),
	       @Index(name = "INDEX_1", columnList = "LINK_ID"),
	})
public class ContentRichMsgDetail extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";

	@Id
	@Column(name = "RICH_DETAIL_ID", columnDefinition="nvarchar(50)")
	private String richDetailId;
	
	@Column(name = "RICH_ID", columnDefinition="nvarchar(50)")
	private String richId;

	@Column(name = "LINK_ID", columnDefinition="nvarchar(50)")
	private String linkId;
	
	@Column(name = "RICH_DETAIL_LETTER", columnDefinition="nvarchar(5)")
	private String richDetailLetter;

	@Column(name = "START_POINT_X")
	private int startPointX;

	@Column(name = "START_POINT_Y")
	private int startPointY;

	@Column(name = "END_POINT_X")
	private int endPointX;

	@Column(name = "END_POINT_Y")
	private int endPointY;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "ACTION_TYPE", columnDefinition="nvarchar(50)")
	private String actionType;

	public String getRichDetailId() {
		return richDetailId;
	}

	public void setRichDetailId(String richDetailId) {
		this.richDetailId = richDetailId;
	}

	public String getRichId() {
		return richId;
	}

	public void setRichId(String richId) {
		this.richId = richId;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	
	public String getRichDetailLetter() {
		return richDetailLetter;
	}

	public void setRichDetailLetter(String richDetailLetter) {
		this.richDetailLetter = richDetailLetter;
	}

	public int getStartPointX() {
		return startPointX;
	}

	public void setStartPointX(int startPointX) {
		this.startPointX = startPointX;
	}

	public int getStartPointY() {
		return startPointY;
	}

	public void setStartPointY(int startPointY) {
		this.startPointY = startPointY;
	}

	public int getEndPointX() {
		return endPointX;
	}

	public void setEndPointX(int endPointX) {
		this.endPointX = endPointX;
	}

	public int getEndPointY() {
		return endPointY;
	}

	public void setEndPointY(int endPointY) {
		this.endPointY = endPointY;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
}
