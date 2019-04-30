package com.bcs.web.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.json.AbstractBcsEntity;

public class ContentRewardCardModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;


	private ContentRewardCard contentRewardCard;
	
	private List<ContentCoupon> contentCouponList;
	
	public ContentRewardCard getContentRewardCard() {
		return contentRewardCard;
	}

	public void setContentRewardCard(ContentRewardCard contentRewardCard) {
		this.contentRewardCard = contentRewardCard;
	}
	
	public List<ContentCoupon> getContentCouponList() {
		return contentCouponList;
	}

	public void setContentCouponList(List<ContentCoupon> contentCouponList) {
		this.contentCouponList = contentCouponList;
	}

}
