package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.RecordReport;
import com.bcs.core.db.persistence.EntityRepository;

public interface RecordReportRepository extends EntityRepository<RecordReport, Long>{

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE (REFERENCE_ID = ?1 OR INCREASE_ID = ?1) AND CONTENT_TYPE = ?2 ", nativeQuery = true)
	public List<RecordReport> findByReferenceIdAndContentType(String referenceId, String contentType);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE (REFERENCE_ID = ?1 OR INCREASE_ID = ?1) AND CONTENT_TYPE = ?2 AND DATA_TYPE = ?3", nativeQuery = true)
	public List<RecordReport> findByReferenceIdAndContentTypeAndDataType(String referenceId, String contentType, String dataType);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE (REFERENCE_ID = ?1 OR INCREASE_ID = ?1) AND CONTENT_TYPE = ?2 AND DATA_TYPE = ?3 AND RECORD_TIME = ?4 ", nativeQuery = true)
	public RecordReport findByReferenceIdAndContentTypeAndDataTypeAndRecordTime(String referenceId, String contentType, String dataType, Date recordTime);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE RECORD_TIME >= ?1 AND RECORD_TIME < ?2", nativeQuery = true)
	public List<RecordReport> findRecordReportListByRecordTime(String startTimeStr, String endTimeStr);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE RECORD_TIME >= ?1 AND RECORD_TIME < ?2 AND (REFERENCE_ID = ?3 OR INCREASE_ID = ?3)", nativeQuery = true)
	public List<RecordReport> findRecordReportListByRecordTimeAndReferenceId(String startTimeStr, String endTimeStr, String referenceId);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE RECORD_TIME >= ?1 AND RECORD_TIME < ?2 AND CONTENT_TYPE = ?3", nativeQuery = true)
	public List<RecordReport> findRecordReportListByRecordTimeAndContentType(String startTimeStr, String endTimeStr, String contentType);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE RECORD_TIME >= ?1 AND RECORD_TIME < ?2 AND DATA_TYPE = ?3", nativeQuery = true)
	public List<RecordReport> findRecordReportListByRecordTimeAndDataType(String startTimeStr, String endTimeStr, String dataType);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE (REFERENCE_ID = ?1 OR INCREASE_ID = ?1) AND CONTENT_TYPE = ?2  AND RECORD_TIME >= ?3 AND RECORD_TIME < ?4", nativeQuery = true)
	public List<RecordReport> findRecordReportListByRecordTime(String referenceId, String contentType, String startTimeStr, String endTimeStr);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * "
			+ "FROM BCS_RECORD_REPORT "
			+ "WHERE (REFERENCE_ID = ?1 OR INCREASE_ID = ?1) AND CONTENT_TYPE = ?2 AND DATA_TYPE = ?3 AND RECORD_TIME >= ?4 AND RECORD_TIME < ?5", nativeQuery = true)
	public List<RecordReport> findRecordReportListByRecordTime(String referenceId, String contentType, String dataType, String startTimeStr, String endTimeStr);
}
