package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "BCS_USER_FIELD_SET",
	indexes = {
	       @Index(name = "INDEX_0", columnList = "NAME"),
	       @Index(name = "INDEX_1", columnList = "KEY_DATA"),
	       @Index(name = "INDEX_2", columnList = "MID") ,
	       @Index(name = "INDEX_3", columnList = "TYPE") ,
	})
public class UserFieldSet extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "FIELD_ID")
	private Long fieldId;

	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;
	
	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "SET_TIME")
	private Date setTime;

	@Column(name = "NAME", columnDefinition="nvarchar(50)")
	private String name;

	@Column(name = "KEY_DATA", columnDefinition="nvarchar(50)")
	private String keyData;

	@Column(name = "VALUE", columnDefinition="nvarchar(200)")
	private String value;
    
    @Column(name = "TYPE", columnDefinition="nvarchar(10)")
    private String type;
    
    @Column(name = "FORMAT", columnDefinition="nvarchar(50)")
    private String format;

	public Long getFieldId() {
		return fieldId;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public Date getSetTime() {
		return setTime;
	}

	public void setSetTime(Date setTime) {
		this.setTime = setTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKeyData() {
		return keyData;
	}

	public void setKeyData(String keyData) {
		this.keyData = keyData;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
