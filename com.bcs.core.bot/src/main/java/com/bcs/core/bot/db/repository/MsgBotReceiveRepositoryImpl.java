package com.bcs.core.bot.db.repository;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;

import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.repository.EntityManagerControl;
@Repository
public class MsgBotReceiveRepositoryImpl  implements MsgBotReceiveRepositoryCustom {

	/** Logger */
	private static Logger logger = Logger.getLogger(MsgBotReceiveRepositoryImpl.class);
	
	@Autowired
	private EntityManagerControl entityManagerControl;
	
	@PersistenceContext
	private EntityManager entityManager;

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

	@Transactional
	@Modifying
	public void updateStatus(String detailTable, String detailId) {
		String queryString = 
			"update " + detailTable + " set STATUS = 'COMPLETE', MODIFY_TIME = GETDATE(), PNP_DELIVERY_TIME = GETDATE()"
				+ " where PNP_DETAIL_ID = '" + detailId + "';";
		logger.info("queryString:"+queryString);
		int updateNum = entityManager.createNativeQuery(queryString).executeUpdate();
		logger.info("updateNum:"+updateNum);
	}
	
	
}
