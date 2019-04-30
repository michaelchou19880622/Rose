package com.bcs.core.taishin.api.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class UpdateStatusModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String uid;
	
	private Long time;
	
	private String status;
	
	private List<UpdateStatusFieldModel> field = new ArrayList<UpdateStatusFieldModel>();

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<UpdateStatusFieldModel> getField() {
		return field;
	}

	public void setField(List<UpdateStatusFieldModel> field) {
		this.field = field;
	}
}
