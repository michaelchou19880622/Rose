package com.bcs.core.bot.db.repository;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.repository.EntityManagerControl;

@Repository
public class MsgBotReceiveRepositoryImpl  implements MsgBotReceiveRepositoryCustom {

	@Autowired
	private EntityManagerControl entityManagerControl;

	@Override
	public void bulkPersist(List<MsgBotReceive> msgReceives) {

		if (CollectionUtils.isEmpty(msgReceives)) {
			return;
		}
		
		for (MsgBotReceive msgReceive : msgReceives) {
			entityManagerControl.persist(msgReceive);
		}
	}

	@Override
	public void bulkPersist(MsgBotReceive msgReceive) {
		entityManagerControl.persist(msgReceive);
	}
}
