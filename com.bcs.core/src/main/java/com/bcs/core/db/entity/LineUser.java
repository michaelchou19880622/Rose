package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_LINE_USER",
indexes = {
	       @Index(name = "INDEX_0", columnList = "STATUS"),
	})
public class LineUser extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String STATUS_BINDED = "BINDED";
	public static final String STATUS_BLOCK = "BLOCK";
	public static final String STATUS_UNBIND = "UNBIND";
	public static final String STATUS_UNFRIEND = "UNFRIEND";

	@Id
	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;

	@Column(name = "SOURCE_TYPE", columnDefinition="nvarchar(50)")
	private String soureType;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "CUSTID", columnDefinition="nvarchar(50)")
	private String custId;
	
	@Column(name = "PHONE", columnDefinition="nvarchar(50)")
	private String phone;
	
	@Column(name = "GENDER", columnDefinition="nvarchar(5)")
	private String gender;
	
	@Column(name = "ADDRESS", columnDefinition="nvarchar(100)")
	private String address;
	
	@Column(name = "CITYDISTRICT", columnDefinition="nvarchar(10)")
	private String cityDistrict;
	
	@Column(name = "HASINV", columnDefinition="nvarchar(10)")
	private String hasInv;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "CREATE_TIME")
	private Date createTime;

    @Column(name = "NAME", columnDefinition="NVARCHAR(50)")
    private String name = null;

    @Column(name = "MOBILE", columnDefinition="NVARCHAR(15)")
    private String mobile = null;

    @Column(name = "EMAIL", columnDefinition="NVARCHAR(100)")
    private String email = null;

    @Column(name = "BIRTHDAY", columnDefinition="NVARCHAR(20)")
    private String birthday = null;
    
	@Column(name = "ISBINDED", columnDefinition="nvarchar(50)")
	private String isBinded;
	
	public LineUser(){
		
	}
	
	public LineUser(String mid){
		this.mid = mid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getCityDistrict() {
		return cityDistrict;
	}

	public void setCityDistrict(String cityDistrict) {
		this.cityDistrict = cityDistrict;
	}
	
	public String getHasInv() {
		return hasInv;
	}

	public void setHasInv(String hasInv) {
		this.hasInv = hasInv;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getSoureType() {
		return soureType;
	}

	public void setSoureType(String soureType) {
		this.soureType = soureType;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	
	public String getIsBinded() {
		return isBinded;
	}

	public void setIsBinded(String isBinded) {
		this.isBinded = isBinded;
	}
	
}
