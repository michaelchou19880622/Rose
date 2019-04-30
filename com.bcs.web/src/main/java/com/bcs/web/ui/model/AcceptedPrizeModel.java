package com.bcs.web.ui.model;

import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class AcceptedPrizeModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	private String userName;
	private String userPhoneNumber;
	private String userEMail;
	private String address;
	private Integer numOfChildren;
	private List<String> preferredProducts;
	private String gender;
	private String birthdayYear;
	private String birthdayMonth;
	private String phoneNum;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPhoneNumber() {
		return userPhoneNumber;
	}
	public void setUserPhoneNumber(String userPhoneNumber) {
		this.userPhoneNumber = userPhoneNumber;
	}
	
	public String getUserEMail() {
		return userEMail;
	}
	
	public void setUserEMail(String userEMail) {
		this.userEMail = userEMail;
	}
	
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public List<String> getPreferredProducts() {
        return preferredProducts;
    }
    
    public void setPreferredProducts(List<String> preferredProducts) {
        this.preferredProducts = preferredProducts;
    }
    
    public Integer getNumOfChildren() {
        return numOfChildren;
    }
    
    public void setNumOfChildren(Integer numOfChildren) {
        this.numOfChildren = numOfChildren;
    }
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getBirthdayYear() {
		return birthdayYear;
	}
	public void setBirthdayYear(String birthdayYear) {
		this.birthdayYear = birthdayYear;
	}
	public String getBirthdayMonth() {
		return birthdayMonth;
	}
	public void setBirthdayMonth(String birthdayMonth) {
		this.birthdayMonth = birthdayMonth;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
}
