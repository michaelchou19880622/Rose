package com.bcs.core.db.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_PRIZE")
public class ContentPrize extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";
	
	@Id
	@Column(name = "PRIZE_ID", columnDefinition="nvarchar(50)")
	private String prizeId;
	
	@Column(name = "GAME_ID")
	private String gameId;
	
	@Column(name = "PRIZE_NAME", columnDefinition="nvarchar(50)")
	private String prizeName;
	
	@Column(name = "PRIZE_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String prizeImageId;
	
	@Column(name = "PRIZE_CONTENT", columnDefinition="nvarchar(500)")
	private String prizeContent;
	
	@Column(name = "PRIZE_QUANTITY")
	private Integer prizeQuantity;
	
	@Column(name = "PRIZE_LETTER", columnDefinition="nvarchar(5)")
	private String prizeLetter;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "PRIZE_PROBABILITY")
	private BigDecimal prizeProbability;
	
	@Column(name = "MSG_DETAIL_ID")
	private Long detailId;
	
	@Column(name = "IS_CONSOLATION_PRIZE")
	private Boolean isConsolationPrize;
	
	public String getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(String prizeId) {
		this.prizeId = prizeId;
	}
	
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public String getPrizeName() {
		return prizeName;
	}

	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}
	
	public String getPrizeImageId() {
		return prizeImageId;
	}

	public void setPrizeImageId(String prizeImageId) {
		this.prizeImageId = prizeImageId;
	}
	
	public String getPrizeContent() {
		return prizeContent;
	}

	public void setPrizeContent(String prizeContent) {
		this.prizeContent = prizeContent;
	}
	
	public Integer getPrizeQuantity() {
		return prizeQuantity;
	}

	public void setPrizeQuantity(Integer prizeQuantity) {
		this.prizeQuantity = prizeQuantity;
	}
	
	public String getPrizeLetter() {
		return prizeLetter;
	}

	public void setPrizeLetter(String prizeLetter) {
		this.prizeLetter = prizeLetter;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public BigDecimal getPrizeProbability() {
		return prizeProbability;
	}

	public void setPrizeProbability(BigDecimal prizeProbability) {
		this.prizeProbability = prizeProbability;
	}

	public Long getDetailId() {
		return detailId;
	}

	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}

	public Boolean getIsConsolationPrize() {
		return isConsolationPrize;
	}

	public void setIsConsolationPrize(Boolean isConsolationPrize) {
		this.isConsolationPrize = isConsolationPrize;
	}
	
}
