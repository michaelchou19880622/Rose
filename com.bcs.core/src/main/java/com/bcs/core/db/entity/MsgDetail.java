package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_MSG_DETAIL",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MSG_ID"),
	       @Index(name = "INDEX_1", columnList = "MSG_PARENT_TYPE"),
	       @Index(name = "INDEX_2", columnList = "MSG_TYPE"),
	       @Index(name = "INDEX_3", columnList = "REFERENCE_ID"),
	})
public class MsgDetail extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "MSG_DETAIL_ID")
	private Long detailId;

	@Column(name = "MSG_ID")
	private Long msgId;

	@Column(name = "MSG_PARENT_TYPE", columnDefinition="nvarchar(50)")
	private String msgParentType;

	@Column(name = "MSG_TYPE", columnDefinition="nvarchar(50)")
	private String msgType;

	@Column(name = "TEXT", columnDefinition="nvarchar(1000)")
	private String text;

    @Column(name = "EVENT_TYPE", columnDefinition="nvarchar(50)")
    private String eventType;

	@Column(name = "REFERENCE_ID", columnDefinition="nvarchar(50)")
	private String referenceId;

	public Long getDetailId() {
		return detailId;
	}

	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}

	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	public String getMsgParentType() {
		return msgParentType;
	}

	public void setMsgParentType(String msgParentType) {
		this.msgParentType = msgParentType;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
	
}
