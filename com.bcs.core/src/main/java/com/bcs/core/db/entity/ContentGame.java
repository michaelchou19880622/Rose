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
@Table(name = "BCS_CONTENT_GAME")
public class ContentGame extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";
	
	public static final String GAME_TYPE_SCRATCH_CARD = "scratchCard";
	public static final String GAME_TYPE_TURNTABLE = "turntable";
	
	@Id
	@Column(name = "GAME_ID", columnDefinition="nvarchar(50)")
	private String gameId;
	
	@Column(name = "GAME_NAME", columnDefinition="nvarchar(50)")
	private String gameName;
	
	@Column(name = "GAME_CONTENT", columnDefinition="nvarchar(500)")
	private String gameContent;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "GAME_TYPE", columnDefinition="nvarchar(50)")
	private String gameType;
	
	@Column(name = "HEADER_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String headerImageId;

	@Column(name = "FOOTER_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String footerImageId;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	
	public String getGameContent() {
		return gameContent;
	}

	public void setGameContent(String gameContent) {
		this.gameContent = gameContent;
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
	
	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getHeaderImageId() {
		return headerImageId;
	}

	public void setHeaderImageId(String headerImageId) {
		this.headerImageId = headerImageId;
	}

	public String getFooterImageId() {
		return footerImageId;
	}

	public void setFooterImageId(String footerImageId) {
		this.footerImageId = footerImageId;
	}
}
