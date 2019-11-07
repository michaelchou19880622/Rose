package com.bcs.core.linepoint.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.db.persistence.EntityRepository;

public interface LinePointDetailRepository extends EntityRepository<LinePointDetail, Long>{

    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointDetail x where x.status = ?1 and x.linePointMainId = ?2 order by x.triggerTime desc")
	public List<LinePointDetail> findByStatusAndLinePointMainId(String status, Long linePointMainId);
    
    @Transactional(timeout = 30)
    public LinePointDetail findByOrderKeyAndStatus(String orderKey, String status);
    
    @Transactional(timeout = 30)
    public List<LinePointDetail> findByLinePointMainId(Long linePointMainId);
    
    @Transactional(timeout = 300)
    @Query(value = "select x from LinePointDetail x where  x.linePointMainId = ?1 "
    			 + "and x.sendTime >= ?2 "
    			 + "and x.sendTime <= ?3 order by x.sendTime desc")
    public List<LinePointDetail> findByLinePointMainIdAndSendDate(Long linePointMainId,Date startDate ,Date endDate);
    
    @Transactional(timeout = 30)
    public List<LinePointDetail> findByDetailId(Long detailId);
    
    @Transactional(timeout = 30)
    public List<LinePointDetail> deleteByLinePointMainId(Long linePointMainId);
    
    @Transactional(timeout = 30)
    @Query(value = "select count(0) from BCS_LINE_POINT_DETAIL where LINE_POINT_MAIN_ID = ?1 and amount > ?2 " ,nativeQuery = true)
    public String getCountLinePointDetailAmountMoreCaveatLinePoint(Long linePointMainId , String caveatLinePoint);
    
    @Transactional(timeout = 30)
    @Query(value =  "select "
    				+		" (select count(status) 	"
    				+		" from BCS_LINE_POINT_DETAIL y "
    				+		" where y.SEND_TIME >=  ?2 "
    				+			" and y.SEND_TIME <= ?3 "
    				+			" and LINE_POINT_MAIN_ID = ?1 and status = 'SUCCESS') as SUCCESS, "
    				+		" (select count(status) 	"
    				+		" from BCS_LINE_POINT_DETAIL y "
    				+		" where y.SEND_TIME >=  ?2 "
    				+			" and y.SEND_TIME <= ?3 "
    				+			" and LINE_POINT_MAIN_ID = ?1 and status = 'FAIL') as FAIL, "
    				+		" (select sum(AMOUNT) 	"
    				+		" from BCS_LINE_POINT_DETAIL y "
    				+		" where y.SEND_TIME >=  ?2 "
    				+			" and y.SEND_TIME <= ?3 "
    				+			" and LINE_POINT_MAIN_ID = ?1 and status = 'SUCCESS') as TotleAmount "
    				+" from BCS_LINE_POINT_MAIN "
    				+" where ID = ?1 " ,nativeQuery = true)
	public List<Object[]> getSuccessandFailCount(Long linePointMain,Date startDate,Date endDate);
    
//	public LinePointDetail findBySerialId(String serialId);
//	
//	public List<LinePointDetail> findByStatus(String status);
//	
//	public List<LinePointDetail> findByMsgLpId(long msgLpId);
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
