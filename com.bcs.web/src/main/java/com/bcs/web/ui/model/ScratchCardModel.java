package com.bcs.web.ui.model;

import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class ScratchCardModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String gameName;
	private String gameContent;
	private String gameType;
	private String scratchCardFrontImageId;
	private String scratchCardBGImageId;
	private String scratchCardStartButtonImageId;
	private String headerImageId;
	private String footerImageId;
	private List<Object> couponList;
	private List<PrizeModel> prizes;
	
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
	
	public String getGameType() {
		return gameType;
	}
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}
	
	public String getScratchCardFrontImageId() {
		return scratchCardFrontImageId;
	}
	public void setScratchCardFrontImageId(String scratchCardFrontImageId) {
		this.scratchCardFrontImageId = scratchCardFrontImageId;
	}
	
	public String getScratchCardBGImageId() {
		return scratchCardBGImageId;
	}
	public void setScratchCardBGImageId(String scratchCardBGImageId) {
		this.scratchCardBGImageId = scratchCardBGImageId;
	}
	
	public String getScratchCardStartButtonImageId() {
		return scratchCardStartButtonImageId;
	}
	public void setScratchCardStartButtonImageId(String scratchCardStartButtonImageId) {
		this.scratchCardStartButtonImageId = scratchCardStartButtonImageId;
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
		
	public List<Object> getCouponList() {
		return couponList;
	}
	public void setCouponList(List<Object> couponList) {
		this.couponList = couponList;
	}
	
	public List<PrizeModel> getPrizes() {
		return prizes;
	}
	public void setPrizes(List<PrizeModel> prizes) {
		this.prizes = prizes;
	}
}
