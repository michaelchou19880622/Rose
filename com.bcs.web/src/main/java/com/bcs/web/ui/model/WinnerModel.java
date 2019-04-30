package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class WinnerModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	private String winnerName;
	private String winnerIdCardNumber;
	private String winnerPhone;
	private String winnerAddress;
	
	public String getWinnerName() {
		return winnerName;
	}
	public void setWinnerName(String winnerName) {
		this.winnerName = winnerName;
	}
	public String getWinnerIdCardNumber() {
		return winnerIdCardNumber;
	}
	public void setWinnerIdCardNumber(String winnerIdCardNumber) {
		this.winnerIdCardNumber = winnerIdCardNumber;
	}
	public String getWinnerPhone() {
		return winnerPhone;
	}
	public void setWinnerPhone(String winnerPhone) {
		this.winnerPhone = winnerPhone;
	}
	public String getWinnerAddress() {
		return winnerAddress;
	}
	public void setWinnerAddress(String winnerAddress) {
		this.winnerAddress = winnerAddress;
	}
	
}
