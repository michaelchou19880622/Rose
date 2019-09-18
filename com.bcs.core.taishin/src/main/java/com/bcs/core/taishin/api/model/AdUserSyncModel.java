package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;

public class AdUserSyncModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String userId;
	private String roles;
	private String name;
	private String email;
	private String telephone;
	private String department;
	private String linebc;

	private String roseline;
	private String pepper;
	private String robot;
	private String desk;
	
	private String gateway;
	
	private boolean viewLimit;
	private boolean canCopy;
	private boolean canSave;
	private boolean canPrinting;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
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
	
	public boolean getViewLimit() {
		return viewLimit;
	}
	public void setViewLimit(boolean viewLimit) {
		this.viewLimit = viewLimit;
	}
	
	public boolean getCanCopy() {
		return canCopy;
	}
	public void setCanCopy(boolean canCopy) {
		this.canCopy = canCopy;
	}
	
	public boolean getCanSave() {
		return canSave;
	}
	public void setCanSave(boolean canSave) {
		this.canSave = canSave;
	}
	
	public boolean getCanPrinting() {
		return canPrinting;
	}
	public void setCanPrinting(boolean canPrinting) {
		this.canPrinting = canPrinting;
	}
	
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
}