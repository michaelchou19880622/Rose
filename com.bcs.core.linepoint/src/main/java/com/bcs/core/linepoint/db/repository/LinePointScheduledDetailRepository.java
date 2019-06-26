package com.bcs.core.linepoint.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface LinePointScheduledDetailRepository extends EntityRepository<LinePointScheduledDetail, Long>{

	@Transactional(timeout = 30)
	@Query(value="select x from LinePointScheduledDetail x where x.linePointMainId = ?1 order by x.modifyTime desc")
	public List<LinePointScheduledDetail> findByLinePointMainId(long linePointMainId);
	
	//public List<LinePointScheduledDetail> findByStatus(String status);
//	public LinePointDetail findBySerialId(String serialId);
//	
//	
//	@Query(value="SELECT * FROM BCS_LINE_POINT_DETAIL WHERE SERIAL_ID = ?1 AND UID is null", nativeQuery=true)
//	public abstract List<LinePointDetail> findBySerialIdAndEmptyUid(String paramString);
//	 
//	@Query(value = "SELECT * FROM BCS_LINE_POINT_DETAIL WHERE MSG_LP_ID = ?1 AND UID is null", nativeQuery = true)
//	public List<LinePointDetail> findByMsgLpIdAndEmptyUid(long msgLpId);
//	
//	@Modifying
//	@Transactional
//	@Query(value = "update BCS_LINE_POINT_DETAIL set UID=:uid, GET_TIME=GetDate(), SOURCE='API' where MSG_LP_ID = :msgLpId and SERIAL_ID= :serialId", nativeQuery = true)
//	public void updateUidByByMsgLpIdAndSerialId(@Param("msgLpId") long msgLpId, @Param("serialId") String serialId, @Param("uid") String uid);	

//	public List<LinePointSend> findByMsgLpId(Long msgLpId);
//	
//	public List<LinePointSend> findByMsgLpIdAndStatus(Long msgLpId, String status);
//	
//	public List<LinePointSend> findByMainId(Long mainId);
//	
//	public List<LinePointSend> findByMsgLpIdAndUidAndStatus(Long msgLpId, String uid, String status);
//	
//	public List<LinePointSend> findByMsgLpIdAndUid(Long msgLpId, String uid);
//	
//	@Query(value="SELECT COUNT(lp.uid) FROM LinePointSend lp WHERE lp.msgLpId = :msgLpId and lp.status = :status ")
//	public int countByMsgLpIdAndStatus(@Param("msgLpId") long msgLpId, @Param("status")  String status);
//	
//	@Modifying
//	@Transactional
//	@Query("update LinePointSend msg set msg.status = :status, msg.responseCode = :responseCode  where msg.msgLpId = :msgLpId and msg.uid= :uid")
//	public void updateStautsAndRespCodeByUidAndId(@Param("msgLpId") long msgLpId, @Param("status")  String status, @Param("responseCode") int responseCode, @Param("uid") String uid);
//	
//	@Modifying
//	@Transactional
//	@Query("update LinePointSend msg set msg.status = :status, msg.responseCode = :responseCode  where msg.msgLpId = :msgLpId" )
//	public void updateStautsAndRespCode(@Param("msgLpId") long msgLpId, @Param("status")  String status, @Param("responseCode") int responseCode);
}
