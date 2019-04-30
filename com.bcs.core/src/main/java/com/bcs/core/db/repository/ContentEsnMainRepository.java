package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.ContentEsnMain;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface ContentEsnMainRepository extends EntityRepository<ContentEsnMain, String> {

	@Modifying
	@Query("update ContentEsnMain c set c.status = ?1 where c.esnId = ?2")
	int updateStatusByEsnId(String status, String esnId);

    @Query(value="SELECT m.ESN_ID, "
            + "m.ESN_NAME, "
            + "m.ESN_MSG, "
            + "FORMAT(m.MODIFY_TIME , 'yyyy-MM-dd HH:mm:ss'), "
            + "m.MODIFY_USER, "
            + "m.SEND_STATUS, "
            + "m.STATUS, "
    		+ "(CASE WHEN m.STATUS = 'ACTIVE'"
    		+ "THEN "
    		+ "(SELECT COUNT(*) FROM BCS_CONTENT_ESN_DETAIL d1 WHERE d1.ESN_ID = m.ESN_ID) "
    		+ "ELSE 0 END) totalCount, "
    		+ "(CASE WHEN m.STATUS = 'ACTIVE' AND (m.SEND_STATUS = 'READY' OR m.SEND_STATUS = 'FINISH') "
    		+ "THEN "
    		+ "(SELECT COUNT(*) FROM BCS_CONTENT_ESN_DETAIL d2 WHERE d2.ESN_ID = m.ESN_ID AND (d2.STATUS = 'FINISH' OR d2.STATUS = 'FAIL')) "
    		+ "ELSE 0 END) finishCount "
    		+ "FROM BCS_CONTENT_ESN_MAIN m "
    		+ "WHERE m.STATUS IN (?1) "
    		+ "ORDER BY m.MODIFY_TIME DESC", nativeQuery=true)
    List<Object[]> findDataByStatus(List<String> statusList);
    
    @Query(value="SELECT m.ESN_ID, "
            + "m.ESN_NAME, "
            + "m.ESN_MSG, "
            + "FORMAT(m.MODIFY_TIME , 'yyyy-MM-dd HH:mm:ss'), "
            + "m.MODIFY_USER, "
            + "m.SEND_STATUS, "
            + "m.STATUS, "
            + "(CASE WHEN m.STATUS = 'ACTIVE'"
            + "THEN "
            + "(SELECT COUNT(*) FROM BCS_CONTENT_ESN_DETAIL d1 WHERE d1.ESN_ID = m.ESN_ID) "
            + "ELSE 0 END) totalCount, "
            + "(CASE WHEN m.STATUS = 'ACTIVE' AND (m.SEND_STATUS = 'READY' OR m.SEND_STATUS = 'FINISH') "
            + "THEN"
            + "(SELECT COUNT(*) FROM BCS_CONTENT_ESN_DETAIL d2 WHERE d2.ESN_ID = m.ESN_ID AND (d2.STATUS = 'FINISH' OR d2.STATUS = 'FAIL')) "
            + "ELSE 0 END) finishCount "
            + "FROM BCS_CONTENT_ESN_MAIN m "
            + "WHERE m.STATUS IN (?1) "
            + "AND m.MODIFY_TIME >= ?2 AND m.MODIFY_TIME < ?3 "
            + "ORDER BY m.MODIFY_TIME DESC", nativeQuery=true)
    List<Object[]> findDataByStatusAndModifyTime(List<String> statusList, Date start, Date end);
	
	@Modifying
    @Query("update ContentEsnMain c set c.sendStatus = ?1 where c.esnId = ?2")
    int updateSendStatusByEsnId(String sendStatus, String esnId);
}
