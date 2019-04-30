package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_PRODUCT_GROUP", indexes = {})
public class ProductGroup extends AbstractBcsEntity{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "GROUP_ID", columnDefinition="NVARCHAR(50)")
    private String groupId = null;

    @Column(name = "GROUP_NAME", columnDefinition="NVARCHAR(200)")
    private String groupName = null;

    @Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
    private String modifyUser;

    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    public ProductGroup() {
        
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
    
    
}
