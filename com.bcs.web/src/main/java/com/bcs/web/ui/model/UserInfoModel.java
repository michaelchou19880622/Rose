package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class UserInfoModel extends AbstractBcsEntity  {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String id_card_number;
	private String phone;
	private String address;
	private String couponId;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId_card_number() {
		return id_card_number;
	}
	public void setId_card_number(String id_card_number) {
		this.id_card_number = id_card_number;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getCouponId() {
		return couponId;
	}
	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}
	
}
