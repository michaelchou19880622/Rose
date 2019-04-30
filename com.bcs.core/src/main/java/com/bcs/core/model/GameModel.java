package com.bcs.core.model;

import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class GameModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	private String gameId;
	private String gameName;
	private String gameContent;
	private String gameType;
	
	private String headerImageId;
	private String footerImageId;
	
	private String turntableImageId;
	private String turntableBackgroundImageId;
	private String pointerImageId;
	
	private String scratchcardBackgroundImageId;
	private String scratchcardFrontImageId;
	private String scratchcardStartButtonImageId;
	
	private List<PrizeModel> prizes;
	private List<CouponModel> couponList;
	private CouponModel drewCoupon;
	private String modifyTime;
	private String modifyUserName;
	
	private String shareImageId;
	private String shareSmallImageId;
	private String shareMsg;
	private String gameProcess;
	private Integer gameLimitCount;
	
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
	
	public String getTurntableImageId() {
		return turntableImageId;
	}
	public void setTurntableImageId(String turntableImageId) {
		this.turntableImageId = turntableImageId;
	}
	
	public String getTurntableBackgroundImageId() {
		return turntableBackgroundImageId;
	}
	public void setTurntableBackgroundImageId(String turntableBackgroundImageId) {
		this.turntableBackgroundImageId = turntableBackgroundImageId;
	}
	
	public String getPointerImageId() {
		return pointerImageId;
	}
	public void setPointerImageId(String pointerImageId) {
		this.pointerImageId = pointerImageId;
	}

	public String getScratchcardBackgroundImageId() {
		return scratchcardBackgroundImageId;
	}
	public void setScratchcardBackgroundImageId(String scratchcardBackgroundImageId) {
		this.scratchcardBackgroundImageId = scratchcardBackgroundImageId;
	}
	
	public String getScratchcardFrontImageId() {
		return scratchcardFrontImageId;
	}
	public void setScratchcardFrontImageId(String scratchcardFrontImageId) {
		this.scratchcardFrontImageId = scratchcardFrontImageId;
	}
	
	public String getScratchcardStartButtonImageId() {
		return scratchcardStartButtonImageId;
	}
	public void setScratchcardStartButtonImageId(String scratchcardStartButtonImageId) {
		this.scratchcardStartButtonImageId = scratchcardStartButtonImageId;
	}
	
	public List<CouponModel> getCouponList() {
		return couponList;
	}
	public void setCouponList(List<CouponModel> couponList) {
		this.couponList = couponList;
	}
	
	public List<PrizeModel> getPrizes() {
		return prizes;
	}
	public void setPrizes(List<PrizeModel> prizes) {
		this.prizes = prizes;
	}
		
	public CouponModel getDrewCoupon() {
		return drewCoupon;
	}
	public void setDrewCoupon(CouponModel drewCoupon) {
		this.drewCoupon = drewCoupon;
	}
	
	public String getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public String getModifyUserName() {
		return modifyUserName;
	}
	public void setModifyUserName(String modifyUserName) {
		this.modifyUserName = modifyUserName;
	}
	public String getShareImageId() {
		return shareImageId;
	}
	public void setShareImageId(String shareImageId) {
		this.shareImageId = shareImageId;
	}
	public String getShareMsg() {
		return shareMsg;
	}
	public void setShareMsg(String shareMsg) {
		this.shareMsg = shareMsg;
	}
	public String getGameProcess() {
		return gameProcess;
	}
	public void setGameProcess(String gameProcess) {
		this.gameProcess = gameProcess;
	}
	public Integer getGameLimitCount() {
		return gameLimitCount;
	}
	public void setGameLimitCount(Integer gameLimitCount) {
		this.gameLimitCount = gameLimitCount;
	}
	public String getShareSmallImageId() {
		return shareSmallImageId;
	}
	public void setShareSmallImageId(String shareSmallImageId) {
		this.shareSmallImageId = shareSmallImageId;
	}
}
