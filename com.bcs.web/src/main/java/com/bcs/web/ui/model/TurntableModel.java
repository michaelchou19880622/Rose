package com.bcs.web.ui.model;

import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class TurntableModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String gameName;
	private String gameContent;
	private String gameType;
	private String turntableImageId;
	private String turntableBGImageId;
	private String pointerImageId;
	private String headerImageId;
	private String footerImageId;
	private List<PrizeModel> prizes;
	private String shareImageId;
	private String shareMsg;
	private String gameProcess;
	private Integer gameLimitCount;
	private String shareSmallImageId;
	
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
	
	public List<PrizeModel> getPrizes() {
		return prizes;
	}
	public void setPrizes(List<PrizeModel> prizes) {
		this.prizes = prizes;
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
	public String getShareMsg() {
		return shareMsg;
	}
	public void setShareMsg(String shareMsg) {
		this.shareMsg = shareMsg;
	}
	public String getShareImageId() {
		return shareImageId;
	}
	public void setShareImageId(String shareImageId) {
		this.shareImageId = shareImageId;
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