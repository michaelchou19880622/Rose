package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_ACCEPTED_PRIZE")
public class ContentAcceptedPrize extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ACCEPTED_PRIZE_ID", columnDefinition="nvarchar(50)")
	private String acceptedPrizeId;
	
	@Column(name = "GAME_ID", columnDefinition="nvarchar(50)")
	private String gameId;
	
	@Column(name = "USER_NAME", columnDefinition="nvarchar(50)")
	private String userName;
	
	@Column(name = "USER_PHONENUMBER", columnDefinition="nvarchar(50)")
	private String userPhoneNumber;
	
	@Column(name = "USER_EMAIL", columnDefinition="nvarchar(50)")
	private String userEMail;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "PRIZE_LIST_ID")
	private Integer prizeListId;
    
    @Column(name = "MID", columnDefinition="nvarchar(50)")
    private String mid;
    
	public String getAcceptedPrizeId() {
		return acceptedPrizeId;
	}

	public void setAcceptedPrizeId(String acceptedPrizeId) {
		this.acceptedPrizeId = acceptedPrizeId;
	}
	
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserPhoneNumber() {
		return userPhoneNumber;
	}

	public void setUserPhoneNumber(String userPhoneNumber) {
		this.userPhoneNumber = userPhoneNumber;
	}

	public String getUserEMail() {
		return userEMail;
	}

	public void setUserEMail(String userEMail) {
		this.userEMail = userEMail;
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public Integer getPrizeListId() {
		return prizeListId;
	}

	public void setPrizeListId(Integer prizeListId) {
		this.prizeListId = prizeListId;
	}

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
