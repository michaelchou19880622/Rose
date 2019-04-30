package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserRewardCardPointDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface ActionUserRewardCardPointDetailRepository extends EntityRepository<ActionUserRewardCardPointDetail, Long> {
	@Query(value = "SELECT SUM(POINT_GET_AMOUNT) FROM BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL WHERE USER_REWARD_CARD_ID = ?1", nativeQuery = true)
	int sumActionUserRewardCardGetPoint(Long actionRewardCardId);
	
	List<ActionUserRewardCardPointDetail> findByUserRewardCardIdAndReferenceId(Long userRewardCardId, String referenceId);
	
	@Transactional(readOnly = true, timeout = 30)
	@Query("select "
			+ "case when count(x) > 0 then true else false end "
		+ "from ActionUserRewardCardPointDetail x "
		+ "WHERE x.userRewardCardId = ?1 "
			+ "and x.rewardCardPointId = ?2 ")
	public boolean existsByUserRewardCardIdAndRewardCardPointId(Long userRewardCardId, String rewardCardPointId);

	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "select case when "
            + "(select count(*) "
            + "from BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL rcpd "
            + "where rcpd.POINT_GET_TIME > DATEADD(hh, -(crc.REWARDCARD_LIMIT_GET_TIME), ?2) "
            + "and rcpd.REFERENCE_ID = ?1 and rcpd.POINT_GET_AMOUNT > 0 and rcpd.POINT_TYPE = ?3) "
            + ">= crc.REWARDCARD_LIMIT_GET_NUMBER and crc.REWARDCARD_LIMIT_GET_NUMBER > 0 "
            + "then cast(1 as bit) else cast(0 as bit) end "
            + "from BCS_CONTENT_REWARD_CARD crc where crc.REWARDCARD_ID = ?1", nativeQuery = true)
    public boolean existsByUserRewardCardIdAndLimitGetNumberAndLimitGetTime(String contentRewardCardId, Date now, String pointType);

	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "select count(*) from BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL where USER_REWARD_CARD_ID = ?1 and REFERENCE_ID = ?2 and POINT_TYPE = ?3 and POINT_GET_TIME >= ?4 and POINT_GET_TIME < ?5", nativeQuery = true)
    public Long countByUserRewardCardIdAndReferenceIdAndPointType(Long userRewardCardId, String referenceId, String pointType, String startDate, String endDate);

//	@Query(value = "SELECT COUNT(*) FROM BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL WHERE REFERENCE_ID = ?1", nativeQuery = true)
	@Query("select count(*) from ActionUserRewardCardPointDetail where referenceId = ?1")
	public Integer countRewardCardPointNumByRewardCardId(String rewardCardId);
}
