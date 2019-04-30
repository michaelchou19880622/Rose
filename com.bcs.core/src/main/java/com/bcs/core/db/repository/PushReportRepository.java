package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.PushReport;
import com.bcs.core.db.persistence.EntityRepository;

public interface PushReportRepository extends EntityRepository<PushReport, Long>{
	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_PUSH_REPORT "
			+ "WHERE REPORT_PUSH_TIME BETWEEN :startDate AND :endDate "
			+ "ORDER BY REPORT_PUSH_TIME DESC", nativeQuery = true)
	public List<PushReport> findByPushTimeOrderByPushTime(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Transactional(readOnly = true, timeout = 30)
	public PushReport findByReportLinkId(String reportLinkId);

	@Transactional(readOnly = true, timeout = 30)
	public PushReport findByReportMsgSendIdAndReportLinkId(Long reportMsgSendId, String reportLinkId);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_PUSH_REPORT "
			+ "WHERE REPORT_RICH_ID = :reportRichId AND REPORT_ID != :reportId", nativeQuery = true)
	public List<PushReport> findByReportRichId(@Param("reportRichId") String reportRichId, @Param("reportId") Long reportId); 

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_PUSH_REPORT "
			+ "WHERE REPORT_MSG_SEND_ID = :reportMsgSendId AND REPORT_ID != :reportId", nativeQuery = true)
	public List<PushReport> findByReportMsgSendId(@Param("reportMsgSendId") Long reportMsgSendId, @Param("reportId") Long reportId); 

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT TOP 1 * "
			+ "FROM BCS_PUSH_REPORT "
			+ "WHERE REPORT_RICH_ID = :reportRichId ORDER BY REPORT_ID DESC", nativeQuery = true)
//	@Query(value = "SELECT * "
//			+ "FROM BCS_PUSH_REPORT "
//			+ "WHERE REPORT_RICH_ID = :reportRichId ORDER BY REPORT_ID DESC  LIMIT 1", nativeQuery = true)// MYSQL Difference
	public PushReport getOnlyOneByReportRichId(@Param("reportRichId") String reportRichId);
}
