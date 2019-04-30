package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_STICKER")
public class ContentSticker extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "STICKER_STKID", columnDefinition="nvarchar(50)")
	private String stickerStkid;

	@Column(name = "STICKER_STKPKGID", columnDefinition="nvarchar(50)")
	private String stickerStkpkgid;

	@Column(name = "STICKER_STKVER", columnDefinition="nvarchar(50)")
	private String stickerStkver;
	
	public ContentSticker(){
		
	}
	
	public ContentSticker(String stickerStkid){
		this.stickerStkid = stickerStkid;
	}

	public String getStickerStkid() {
		return stickerStkid;
	}

	public void setStickerStkid(String stickerStkid) {
		this.stickerStkid = stickerStkid;
	}

	public String getStickerStkpkgid() {
		return stickerStkpkgid;
	}

	public void setStickerStkpkgid(String stickerStkpkgid) {
		this.stickerStkpkgid = stickerStkpkgid;
	}

	public String getStickerStkver() {
		return stickerStkver;
	}

	public void setStickerStkver(String stickerStkver) {
		this.stickerStkver = stickerStkver;
	}
	
}
