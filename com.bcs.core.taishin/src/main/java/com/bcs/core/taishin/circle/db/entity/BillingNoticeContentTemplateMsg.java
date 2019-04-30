package com.bcs.core.taishin.circle.db.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_BN_CONTENT_TEMPLATE")
public class BillingNoticeContentTemplateMsg extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String TEMPLATE_LEVEL_MAIN = "MAIN"; // 暫時用不上
	// For Carousel Type
	public static final String TEMPLATE_LEVEL_COLUMN = "COLUMN";  
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";
	public static final String STATUS_AUTOREPLY = "AUTOREPLY"; // 暫時用不上

	public static final String TEMPLATE_TYPE_BUTTONS = "buttons";
	public static final String TEMPLATE_TYPE_CONFIRM = "confirm";
	public static final String TEMPLATE_TYPE_CAROUSEL = "carousel";
	
	@Id
	@Column(name = "TEMPLATE_ID", columnDefinition="nvarchar(50)")
	private String templateId;

	//產品開關：當單一產品不要發送的時候調整此開關
	@Column(name = "PRODUCT_SWITCH", nullable=false)
	@ColumnDefault("1")
	private boolean productSwitch; 
	
	@Column(name = "ALT_TEXT", columnDefinition="nvarchar(500)")
	private String altText;

	@Column(name = "TEMPLATE_TYPE", columnDefinition="nvarchar(50)")
	private String templateType;

	// 暫時用不上
	@Column(name = "TEMPLATE_LEVEL", columnDefinition="nvarchar(50)")
	private String templateLevel;

	// 暫時用不上
	@Column(name = "TEMPLATE_PARENT_ID", columnDefinition="nvarchar(50)")
	private String templateParentId;

	@Column(name = "TEMPLATE_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String templateImageId;

	//比對ftp檔案header 對應template用
	@Column(name = "TEMPLATE_TITLE", columnDefinition="nvarchar(200)")
	private String templateTitle;
	// 暫時用不上 - BCS_BILLING_NOTICE_DETAIL 內有
	@Column(name = "TEMPLATE_TEXT", columnDefinition="nvarchar(500)")
	private String templateText;
	
	//宵禁起始時間：24小時制 HHMMSS
	@Column(name = "CURFEW_START_TIME", columnDefinition="nvarchar(10)")
	private String curfewStartTime;
	//宵禁結束時間：24小時制 HHMMSS
	@Column(name = "CURFEW_END_TIME", columnDefinition="nvarchar(10)")
	private String curfewEndTime;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	// 暫時用不上 
	@Column(name = "TEMPLATE_LETTER", columnDefinition="nvarchar(5)")
	private String templateLetter;
	
	@PrePersist
	public void prePersist() {
		modifyTime = Calendar.getInstance().getTime();
	}
	
	@PreUpdate
	public void preUpdate() {
		modifyTime = Calendar.getInstance().getTime();
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateLevel() {
		return templateLevel;
	}

	public void setTemplateLevel(String templateLevel) {
		this.templateLevel = templateLevel;
	}

	public String getTemplateParentId() {
		return templateParentId;
	}

	public void setTemplateParentId(String templateParentId) {
		this.templateParentId = templateParentId;
	}

	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTemplateImageId() {
		return templateImageId;
	}

	public void setTemplateImageId(String templateImageId) {
		this.templateImageId = templateImageId;
	}

	public String getTemplateTitle() {
		return templateTitle;
	}

	public void setTemplateTitle(String templateTitle) {
		this.templateTitle = templateTitle;
	}

	public String getTemplateText() {
		return templateText;
	}

	public void setTemplateText(String templateText) {
		this.templateText = templateText;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTemplateLetter() {
		return templateLetter;
	}

	public void setTemplateLetter(String templateLetter) {
		this.templateLetter = templateLetter;
	}

	public boolean isProductSwitch() {
		return productSwitch;
	}

	public void setProductSwitch(boolean productSwitch) {
		this.productSwitch = productSwitch;
	}

	public String getCurfewStartTime() {
		return curfewStartTime;
	}

	public void setCurfewStartTime(String curfewStartTime) {
		this.curfewStartTime = curfewStartTime;
	}

	public String getCurfewEndTime() {
		return curfewEndTime;
	}

	public void setCurfewEndTime(String curfewEndTime) {
		this.curfewEndTime = curfewEndTime;
	}
	
	
}
