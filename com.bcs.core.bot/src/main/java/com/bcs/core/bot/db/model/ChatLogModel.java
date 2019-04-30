package com.bcs.core.bot.db.model;

import java.util.Date;

import com.bcs.core.json.AbstractBcsEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ChatLogModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String from;
	
	private String text;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date timestamp;
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}