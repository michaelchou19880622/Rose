package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_PRIZE_LIST")
public class PrizeList extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String PRIZE_STATUS_WINNED = "winned";
	public static final String PRIZE_STATUS_NOT_WINNED = "notWinned";
	public static final String PRIZE_STATUS_NOT_ACCEPTED = "notAccepted";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PRIZE_LIST_ID")
	private Integer prizeListId;
	
	@Column(name = "CREATE_TIME")
	private Date createTime;
	
	@Column(name = "GAME_ID")
	private String gameId;
	
	@Column(name = "PRIZE_ID", columnDefinition="nvarchar(50)")
	private String prizeId;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
    
    @Column(name = "MID", columnDefinition="nvarchar(50)")
    private String mid;
	
	public Integer getPrizeListId() {
		return prizeListId;
	}

	public void setPrizeListId(Integer prizeListId) {
		this.prizeListId = prizeListId;
	}
	
	public String getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(String prizeId) {
		this.prizeId = prizeId;
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
}
