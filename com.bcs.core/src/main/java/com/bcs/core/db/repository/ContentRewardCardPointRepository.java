package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentRewardCardPoint;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentRewardCardPointRepository extends EntityRepository<ContentRewardCardPoint, String> {

	public List<ContentRewardCardPoint> findByStatus(String status);
	
	public List<ContentRewardCardPoint> findByRewardCardId(Long rewardCardId);

	public List<ContentRewardCardPoint> findByRewardCardIdAndStatus(String rewardCardId, String status);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "select * from BCS_CONTENT_REWARD_CARD_POINT where REWARDCARD_POINT_ID = ?1 and STATUS = ?2", nativeQuery = true)
	ContentRewardCardPoint findByIdAndStatus(String id, String status);
	
	@Modifying(clearAutomatically = true)
	@Query(value = "update BCS_CONTENT_REWARD_CARD_POINT set STATUS = ?3 where REWARDCARD_ID = ?1 and STATUS = ?2", nativeQuery = true)
	void updateStatusByRewardCardIdAndStatus(String rewardCardId, String oldStatus, String newStatus);
}
