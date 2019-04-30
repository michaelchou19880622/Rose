package com.bcs.core.model;

import java.math.BigDecimal;

import com.bcs.core.json.AbstractBcsEntity;

public class PrizeModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private String prizeId;
	
	private String prizeName;
	private String prizeImageId;
	private String prizeContent;
	private Integer prizeQuantity;
	private BigDecimal prizeProbability;
	private String messageText;
	private Integer winnedCount;
	private boolean isConsolationPrize;
	
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
	
	public BigDecimal getPrizeProbability() {
		return prizeProbability;
	}
	public void setPrizeProbability(BigDecimal prizeProbability) {
		this.prizeProbability = prizeProbability;
	}
	
	public String getPrizeId() {
		return prizeId;
	}
	public void setPrizeId(String prizeId) {
		this.prizeId = prizeId;
	}
	
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	
	public Integer getWinnedCount() {
		return winnedCount;
	}
	public void setWinnedCount(Integer winnedCount) {
		this.winnedCount = winnedCount;
	}

	public boolean getIsConsolationPrize() {
		return isConsolationPrize;
	}

	public void setIsConsolationPrize(boolean isConsolationPrize) {
		this.isConsolationPrize = isConsolationPrize;
	}
}
