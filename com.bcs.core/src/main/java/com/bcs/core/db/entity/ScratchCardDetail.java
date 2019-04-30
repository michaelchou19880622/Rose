package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
@Entity
@Table(name = "BCS_SCRATCHCARD_DETAIL")
public class ScratchCardDetail extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "SCRATCHCARD_DETAIL_ID", columnDefinition="nvarchar(50)")
	private String scratchCardDetailId;
	
	@Column(name = "GAME_ID")
	private String gameId;
	
	@Column(name = "SCRATCHCARD_BACKGROUND_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String scratchCardBGImageId;
	
	@Column(name = "SCRATCHCARD_FRONT_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String scratchCardFrontImageId;
	
	@Column(name = "SCRATCHCARD_START_BUTTON_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String scratchCardStartButtonImageId;
	
	public String getScratchCardDetailId() {
		return scratchCardDetailId;
	}

	public void setScratchCardDetailId(String scratchCardDetailId) {
		this.scratchCardDetailId = scratchCardDetailId;
	}
	
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public String getScratchCardBGImageId() {
		return scratchCardBGImageId;
	}

	public void setScratchCardBGImageId(String scratchCardBGImageId) {
		this.scratchCardBGImageId = scratchCardBGImageId;
	}
	
	public String getScratchCardFrontImageId() {
		return scratchCardFrontImageId;
	}
	
	public void setScratchCardFrontImageId(String scratchCardFrontImageId) {
		this.scratchCardFrontImageId = scratchCardFrontImageId;
	}

	public String getScratchCardStartButtonImageId() {
		return scratchCardStartButtonImageId;
	}
	
	public void setScratchCardStartButtonImageId(String scratchCardStartButtonImageId) {
		this.scratchCardStartButtonImageId = scratchCardStartButtonImageId;
	}
}