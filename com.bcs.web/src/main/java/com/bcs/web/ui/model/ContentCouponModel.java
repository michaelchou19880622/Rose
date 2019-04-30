package com.bcs.web.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.json.AbstractBcsEntity;

public class ContentCouponModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private List<String> flagValueList = new ArrayList<String>();

	private ContentCoupon contentCoupon;
	
	private String eventReferenceTitle;
	
	public List<String> getFlagValueList() {
		return flagValueList;
	}

	public void setFlagValueList(List<String> flagValueList) {
		this.flagValueList = flagValueList;
	}

	public ContentCoupon getContentCoupon() {
		return contentCoupon;
	}

	public void setContentCoupon(ContentCoupon contentCoupon) {
		this.contentCoupon = contentCoupon;
	}

	public String getEventReferenceTitle() {
		return eventReferenceTitle;
	}

	public void setEventReferenceTitle(String eventReferenceTitle) {
		this.eventReferenceTitle = eventReferenceTitle;
	}
}

