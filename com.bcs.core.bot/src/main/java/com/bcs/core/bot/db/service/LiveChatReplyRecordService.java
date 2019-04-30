package com.bcs.core.bot.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.LiveChatReplyRecord;
import com.bcs.core.bot.db.repository.LiveChatReplyRecordRepository;

@Service
public class LiveChatReplyRecordService {
	@Autowired
	private LiveChatReplyRecordRepository liveChatReplyRecordRepository;
	
	public LiveChatReplyRecord save(LiveChatReplyRecord record) {
		return liveChatReplyRecordRepository.save(record);
	}
}
