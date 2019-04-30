package com.bcs.core.api.service.model;

import com.bcs.core.json.AbstractBcsEntity;

public class LocationModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	private String longitude;
	private String latitude;
	private String address;
	
	public LocationModel() {}
	
	public LocationModel(String address, String longitude, String latitude) {
		this.address = address;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}