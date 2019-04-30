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
@Table(name = "BCS_MSG_INTERACTIVE_DETAIL",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MSG_INTERACTIVE_ID"),
	})
public class MsgInteractiveDetail extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "MSG_INTERACTIVE_DETAIL_ID")
	private Long iMsgDetailId;

	@Column(name = "MSG_INTERACTIVE_ID")
	private Long iMsgId;

	@Column(name = "OTHER_KEYWORD", columnDefinition="nvarchar(50)")
	private String otherKeyword;

	public Long getiMsgDetailId() {
		return iMsgDetailId;
	}

	public void setiMsgDetailId(Long iMsgDetailId) {
		this.iMsgDetailId = iMsgDetailId;
	}

	public Long getiMsgId() {
		return iMsgId;
	}

	public void setiMsgId(Long iMsgId) {
		this.iMsgId = iMsgId;
	}

	public String getOtherKeyword() {
		return otherKeyword;
	}

	public void setOtherKeyword(String otherKeyword) {
		this.otherKeyword = otherKeyword;
	}
	
}
