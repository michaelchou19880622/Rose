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

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.google.common.collect.Lists;

@Repository
public class PnpDetailMingRepositoryCustomImpl implements PnpDetailMingRepositoryCustom {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpDetailMingRepositoryCustomImpl.class);
	
	
	
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	public void batchInsertPnpDetailMing(final List<PnpDetailMing> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
		String INSERT = "INSERT INTO BCS_PNP_DETAIL_MING" + 
				"(CREAT_TIME, LINE_PUSH_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE],"
				+ " STATUS, UID, SN, VARIABLE1, VARIABLE2, ACCOUNT1, ACCOUNT2, DETAIL_SCHEDULE_TIME, KEEP_SECOND)" + 
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		logger.info("batchInsertPnpDetailMing start");
		List<List<PnpDetailMing>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
		 for(final List<PnpDetailMing> batch : batchLists) {  
			 jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						PnpDetailMing detail = batch.get(i);
						ps.setTimestamp(1, now);
						ps.setTimestamp(2, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
						ps.setTimestamp(3, now);
						ps.setString(4, detail.getMsg());
						ps.setString(5, detail.getPhone());
						ps.setString(6, detail.getPhoneHash());
						ps.setLong(7, detail.getPnpMainId());
						ps.setTimestamp(8, detail.getPnpTime() != null ? new Timestamp(detail.getPnpTime().getTime()) : null);
						ps.setString(9, detail.getProcFlow());
						ps.setString(10, detail.getProcStage());
						ps.setTimestamp(11, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null );
						ps.setTimestamp(12, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
						ps.setString(13, detail.getSource());
						ps.setString(14, detail.getStatus());
						ps.setString(15, detail.getUid());
						ps.setString(16, detail.getSN());
						ps.setString(17, detail.getVariable1());
						ps.setString(18, detail.getVariable2());
						ps.setString(19, detail.getAccount1());
						ps.setString(20, detail.getAccount2());
						ps.setString(21, detail.getDetailScheduleTime());
						ps.setString(22, detail.getKeepSecond());
					}
					@Override
					public int getBatchSize() {
						return batch.size();
					}
				});
		 }
		logger.info("batchInsertPnpDetailMing end");
	}

}
