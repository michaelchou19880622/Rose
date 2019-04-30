package com.bcs.core.bot.db.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.BotReplyRecord;
import com.bcs.core.bot.db.model.ChatLogModel;
import com.bcs.core.bot.db.repository.BotReplyRecordRepository;

@Service
public class BotReplyRecordService {
	@Autowired
	private BotReplyRecordRepository botReplyRecordRepository;

	@PersistenceContext
	EntityManager entityManager;

	public BotReplyRecord save(BotReplyRecord record) {
		return botReplyRecordRepository.save(record);
	}

	/*
	 * ===== 以轉接客服的時間點為基準，拿前 24 小時則對話紀錄 =====
	 */
	public List<ChatLogModel> getChatLog(String UID, Integer hour) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		Date yesterday = DateUtils.addHours(now, hour * -1);
		List<ChatLogModel> chatLogList = new ArrayList<ChatLogModel>();
		
		Query query = entityManager.createNamedQuery("getChatLog").setParameter(1, yesterday).setParameter(2, now).setParameter(3, UID);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for (Object[] o : resultList) {
			ChatLogModel chatLogModel = new ChatLogModel();
			
			chatLogModel.setFrom((o[0] == null) ? null : o[0].toString());
			chatLogModel.setTimestamp((o[3] == null) ? null : dateFormat.parse(o[3].toString()));

			switch(o[1].toString()) {
				case BotReplyRecord.MESSAGE_TYPE_STICKER:
					chatLogModel.setText("[貼圖]");
					break;
				case BotReplyRecord.MESSAGE_TYPE_IMAGE:
					chatLogModel.setText("[圖片]");
					break;
				case BotReplyRecord.MESSAGE_TYPE_VIDEO:
					chatLogModel.setText("[影片]");
					break;
				case BotReplyRecord.MESSAGE_TYPE_AUDIO:
					chatLogModel.setText("[音訊]");
					break;
				case BotReplyRecord.MESSAGE_TYPE_IMAGEMAP:
					chatLogModel.setText("[圖文訊息]");
					break;
				default:
					chatLogModel.setText((o[2] == null) ? null : o[2].toString());
			}
			
			chatLogList.add(chatLogModel);
		}
		
		Collections.reverse(chatLogList);

		return chatLogList;
	}
}