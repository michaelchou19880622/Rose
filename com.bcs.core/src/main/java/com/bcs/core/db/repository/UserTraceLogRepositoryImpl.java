package com.bcs.core.db.repository;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.UserTraceLog;

@Repository
public class UserTraceLogRepositoryImpl  implements UserTraceLogRepositoryCustom {

	@Autowired
	private EntityManagerControl entityManagerControl;

	@Override
	public void bulkPersist(List<UserTraceLog> userTraceLogs) {

		if (CollectionUtils.isEmpty(userTraceLogs)) {
			return;
		}
		
		for (UserTraceLog userTraceLog : userTraceLogs) {
			entityManagerControl.persist(userTraceLog);
		}
	}

	@Override
	public void bulkPersist(UserTraceLog userTraceLog) {
		entityManagerControl.persist(userTraceLog);
	}
}
