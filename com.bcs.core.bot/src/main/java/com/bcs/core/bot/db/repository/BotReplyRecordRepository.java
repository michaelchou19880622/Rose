package com.bcs.core.bot.db.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.bot.db.entity.BotReplyRecord;
import com.bcs.core.db.persistence.EntityRepository;

public interface BotReplyRecordRepository extends EntityRepository<BotReplyRecord, String> {

	@Transactional(readOnly = true, timeout = 300)
	@Query(value = "SELECT * FROM BCS_BOT_REPLY_RECORD WHERE REPLY_TOKEN = ?1", nativeQuery = true)
	BotReplyRecord findByReplyToken(String replyToken);
	
}


