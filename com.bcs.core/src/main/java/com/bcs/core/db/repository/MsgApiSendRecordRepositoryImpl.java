package com.bcs.core.db.repository;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.MsgApiSendRecord;

@Repository
public class MsgApiSendRecordRepositoryImpl  implements MsgApiSendRecordRepositoryCustom {

	@Autowired
	private EntityManagerControl entityManagerControl;

	@Override
	public void bulkPersist(List<MsgApiSendRecord> msgApiSendRecords) {

		if (CollectionUtils.isEmpty(msgApiSendRecords)) {
			return;
		}
		
		for (MsgApiSendRecord msgApiSendRecord : msgApiSendRecords) {
			entityManagerControl.persist(msgApiSendRecord);
		}
	}

	@Override
	public void bulkPersist(MsgApiSendRecord msgApiSendRecord) {
		entityManagerControl.persist(msgApiSendRecord);
	}
}
