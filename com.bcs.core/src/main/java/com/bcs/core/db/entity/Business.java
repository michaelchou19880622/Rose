package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name="BCS_BUSINESS")
public class Business extends AbstractBcsEntity{
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name="ID", nullable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="BIZ_ID", columnDefinition="nvarchar(10)")
    private String bizId;
    
    @Column(name="MODIFY_TIME")
    private Date modifyTime;
    
    @Column(name="MODIFY_USER", columnDefinition="nvarchar(50)")
    private String modifyUser;
    
    @Column(name="GROUP_ID", columnDefinition="nvarchar(50)")
    private String groupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
