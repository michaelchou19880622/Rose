package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_TURNTABLE_DETAIL")
public class TurntableDetail extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "TURNTABLE_DETAIL_ID", columnDefinition="nvarchar(50)")
	private String turntableDetailId;
	
	@Column(name = "GAME_ID")
	private String gameId;
	
	@Column(name = "TURNTABLE_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String turntableImageId;
	
	@Column(name = "TURNTABLE_BACKGROUND_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String turntableBGImageId;
	
	@Column(name = "POINTER_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String pointerImageId;
	
	public String getTurntableDetailId() {
		return turntableDetailId;
	}

	public void setTurntableDetailId(String turntableDetailId) {
		this.turntableDetailId = turntableDetailId;
	}
	
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public String getTurntableImageId() {
		return turntableImageId;
	}

	public void setTurntableImageId(String turntableImageId) {
		this.turntableImageId = turntableImageId;
	}
	
	public String getTurntableBGImageId() {
		return turntableBGImageId;
	}

	public void setTurntableBGImageId(String turntableBGImageId) {
		this.turntableBGImageId = turntableBGImageId;
	}
	
	public String getPointerImageId() {
		return pointerImageId;
	}

	public void setPointerImageId(String pointerImageId) {
		this.pointerImageId = pointerImageId;
	}
}
