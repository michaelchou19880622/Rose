package com.bcs.core.taishin.circle.PNP.db.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.google.common.collect.Lists;

@Repository
public class PnpDetailUnicaRepositoryCustomImpl implements PnpDetailUnicaRepositoryCustom {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpDetailUnicaRepositoryCustomImpl.class);
	
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	public void batchInsertPnpDetailUnica(final List<PnpDetailUnica> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
		String INSERT = "INSERT INTO BCS_PNP_DETAIL_UNICA" + 
				"(CREAT_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME,  PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE], "
				+ "STATUS, UID, LINE_PUSH_TIME, CAMPAIGN_ID, DEST_NAME, PID, PROGRAM_ID, SN, SEGMENT_ID, VARIABLE1, VARIABLE2)" + 
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		logger.info("batchInserPnpDetailUnica start");
		List<List<PnpDetailUnica>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
		 for(final List<PnpDetailUnica> batch : batchLists) {  
			 jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						PnpDetailUnica detail = batch.get(i);
						ps.setTimestamp(1, now);
						ps.setTimestamp(2, now);
						ps.setString(3, detail.getMsg());
						ps.setString(4, detail.getPhone());
						ps.setString(5, detail.getPhoneHash());
						ps.setLong(6, detail.getPnpMainId());
						ps.setTimestamp(7, detail.getPnpTime() != null ? new Timestamp(detail.getPnpTime().getTime()) : null);
						ps.setString(8, detail.getProcFlow());
						ps.setString(9, detail.getProcStage());
						ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null );
						ps.setTimestamp(11, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
						ps.setString(12, detail.getSource());
						ps.setString(13, detail.getStatus());
						ps.setString(14, detail.getUid());
						ps.setTimestamp(15, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
						ps.setString(16, detail.getCampaignID());
						ps.setString(17, detail.getDestName());
						ps.setString(18, detail.getPID());
						ps.setString(19, detail.getProgramID());
						ps.setString(20, detail.getSN());
						ps.setString(21, detail.getSegmentID());
						ps.setString(22, detail.getVariable1());
						ps.setString(23, detail.getVariable2());
					}
					@Override
					public int getBatchSize() {
						return batch.size();
					}
				});
		 }
		logger.info("batchInsertPnpDetailUnica end");
	}
	
}
