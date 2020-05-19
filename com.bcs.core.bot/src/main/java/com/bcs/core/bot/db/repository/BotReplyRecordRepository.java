package com.bcs.core.bot.db.repository;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.bot.db.entity.BotReplyRecord;
import com.bcs.core.db.persistence.EntityRepository;

public interface BotReplyRecordRepository extends EntityRepository<BotReplyRecord, String> {
	

	@Transactional(readOnly = true, timeout = 300)
	BotReplyRecord findByReplyToken(String replyToken);
	
}


