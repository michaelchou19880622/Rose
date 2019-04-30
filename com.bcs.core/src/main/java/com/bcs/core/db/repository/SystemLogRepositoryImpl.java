package com.bcs.core.db.repository;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.SystemLog;

@Repository
public class SystemLogRepositoryImpl  implements SystemLogRepositoryCustom {

	@Autowired
	private EntityManagerControl entityManagerControl;

	@Override
	public void bulkPersist(List<SystemLog> systemLogs) {

		if (CollectionUtils.isEmpty(systemLogs)) {
			return;
		}
		
		for (SystemLog systemLog : systemLogs) {
			entityManagerControl.persist(systemLog);
		}
	}

	@Override
	public void bulkPersist(SystemLog systemLog) {
		entityManagerControl.persist(systemLog);
	}
}
