package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_ADMIN_USER")
public class AdminUser extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public enum RoleCode{
		ROLE_ADMIN("ROLE_ADMIN","管理者", "manager"),
		ROLE_EDIT("ROLE_EDIT", "編輯人員", "editor"),
		ROLE_MARKET("ROLE_MARKET", "行銷人員", "marketing"),
		;
		
		private String roleId;
		private String roleName;
		private String roleNameEn;
		
		RoleCode(String roleId, String roleName, String roleNameEn) {
	        this.setRoleId(roleId);
	        this.setRoleName(roleName);
	        this.setRoleNameEn(roleNameEn);
	    }

		public String getRoleId() {
			return roleId;
		}

		public void setRoleId(String roleId) {
			this.roleId = roleId;
		}

		public String getRoleName() {
			return roleName;
		}

		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}
		public String toString() {
			return roleId;
		}

		public String getRoleNameEn() {
			return roleNameEn;
		}

		public void setRoleNameEn(String roleNameEn) {
			this.roleNameEn = roleNameEn;
		}
	}
	
	@Id
	@Column(name = "ACCOUNT", columnDefinition="nvarchar(50)")
	private String account;

	@Column(name = "USER_NAME", columnDefinition="nvarchar(50)")
	private String userName;

	@Column(name = "DEPARTMENT", columnDefinition="nvarchar(50)")
	private String department;

	@Column(name = "EMAIL", columnDefinition="nvarchar(50)")
	private String email;

	@Column(name = "TELEPHONE", columnDefinition="nvarchar(50)")
	private String telephone;

	@Column(name = "ROLES", columnDefinition="nvarchar(50)")
	private String roles;

	@Column(name = "LINEBC", columnDefinition="nvarchar(50)")
	private String linebc;
	
	@Column(name = "ROSELINE", columnDefinition="nvarchar(50)")
	private String roseline;
    
	@Column(name = "PEPPER", columnDefinition="nvarchar(50)")
	private String pepper;

	@Column(name = "ROBOT", columnDefinition="nvarchar(50)")
	private String robot;

	@Column(name = "DESK", columnDefinition="nvarchar(50)")
	private String desk;

	@Column(name = "PASSWORD", columnDefinition="nvarchar(200)")
	private String password;

	@Column(name = "ROLE", columnDefinition="nvarchar(50)")
	private String role;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;
	
	@Column(name = "VIEW_LIMIT")
	private Boolean viewLimit;
	
	@Column(name = "CAN_COPY")
	private Boolean canCopy;
	
	@Column(name = "CAN_SAVE")
	private Boolean canSave;
	
	@Column(name = "CAN_PRINTING")
	private Boolean canPrinting;

	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
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

	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getLinebc() {
		return linebc;
	}
	public void setLinebc(String linebc) {
		this.linebc = linebc;
	}

    public String getRoseline() {
        return roseline;
    }
    public void setRoseline(String roseline) {
        this.roseline = roseline;
    }
    
	public String getPepper() {
		return pepper;
	}
	public void setPepper(String pepper) {
		this.pepper = pepper;
	}

	public String getRobot() {
		return robot;
	}
	public void setRobot(String robot) {
		this.robot = robot;
	}

	public String getDesk() {
		return desk;
	}
	public void setDesk(String desk) {
		this.desk = desk;
	}

	public Boolean getViewLimit() {
		return viewLimit;
	}
	public void setViewLimit(Boolean viewLimit) {
		this.viewLimit = viewLimit;
	}

	public Boolean getCanCopy() {
		return canCopy;
	}
	public void setCanCopy(Boolean canCopy) {
		this.canCopy = canCopy;
	}

	public Boolean getCanSave() {
		return canSave;
	}
	public void setCanSave(Boolean canSave) {
		this.canSave = canSave;
	}

	public Boolean getCanPrinting() {
		return canPrinting;
	}
	public void setCanPrinting(Boolean canPrinting) {
		this.canPrinting = canPrinting;
	}
	
}
