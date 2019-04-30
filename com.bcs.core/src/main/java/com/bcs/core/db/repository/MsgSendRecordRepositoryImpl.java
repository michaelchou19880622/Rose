package com.bcs.core.db.repository;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.MsgSendRecord;

@Repository
public class MsgSendRecordRepositoryImpl  implements MsgSendRecordRepositoryCustom {

	@Autowired
	private EntityManagerControl entityManagerControl;

	@Override
	public void bulkPersist(List<MsgSendRecord> msgSendRecords) {

		if (CollectionUtils.isEmpty(msgSendRecords)) {
			return;
		}
		
		for (MsgSendRecord msgSendRecord : msgSendRecords) {
			entityManagerControl.persist(msgSendRecord);
		}
	}

	@Override
	public void bulkPersist(MsgSendRecord msgSendRecord) {
		entityManagerControl.persist(msgSendRecord);
	}
}
