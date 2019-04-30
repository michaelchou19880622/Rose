package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class PrizeModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String prizeName;
	private String prizeImageId;
	private String prizeContent;
	private Integer prizeQuantity;
	private String prizeLetter;
	private String prizeProbability;
	private String message;
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
	
	public String getPrizeLetter() {
		return prizeLetter;
	}
	public void setPrizeLetter(String prizeLetter) {
		this.prizeLetter = prizeLetter;
	}
	
	public String getPrizeProbability() {
		return prizeProbability;
	}
	public void setPrizeProbability(String prizeProbability) {
		this.prizeProbability = prizeProbability;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean getIsConsolationPrize() {
		return isConsolationPrize;
	}
	public void setIsConsolationPrize(boolean isConsolationPrize) {
		this.isConsolationPrize = isConsolationPrize;
	}
	
}
