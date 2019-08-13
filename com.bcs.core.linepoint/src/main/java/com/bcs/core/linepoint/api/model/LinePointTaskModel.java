package com.bcs.core.linepoint.api.model;

import java.util.Date;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.linepoint.db.entity.LinePointDetail;

public class LinePointTaskModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private LinePointDetail linePointDetail;
	private String date;
	
	public LinePointDetail getLinePointDetail() {
		return linePointDetail;
	}
	public void setLinePointDetail(LinePointDetail linePointDetail) {
		this.linePointDetail = linePointDetail;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}