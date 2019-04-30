package com.bcs.core.db.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.repository.UserLiveChatRepository;

@Service
public class UserLiveChatService {
	@Autowired
	private UserLiveChatRepository userLiveChatRepository;
	
	public UserLiveChat findOne(Long id) {
		return userLiveChatRepository.findOne(id);
	}
	
	public UserLiveChat findByUID(String UID) {
		return userLiveChatRepository.findByUID(UID);
	}
	
	public UserLiveChat findByChatId(Long chatId) {
		return userLiveChatRepository.findByChatId(chatId);
	}
	
	public List<UserLiveChat> findByStatus(String status){
		return userLiveChatRepository.findByStatus(status);
	}
	
	public List<UserLiveChat> findByUIDAndState(String UID, String status) {
		return userLiveChatRepository.findByUIDAndState(UID, status);
	}
	
	public List<UserLiveChat> findWaitingAndInProgressUser(){
		return userLiveChatRepository.findWaitingAndInProgressUser();
	}
	
	public UserLiveChat findWaitingAndInProgressByUID(String UID){
		return userLiveChatRepository.findWaitingAndInProgressByUID(UID);
	}
	
	public UserLiveChat findByUIDAndStatus(String UID, String status) {
		return userLiveChatRepository.findByUIDAndStatus(UID, status);
	}

	public UserLiveChat save(UserLiveChat userLiveChat) {
		return userLiveChatRepository.save(userLiveChat);
	}
	
	public String findUIDByChatIdAndHash(Long chatId, String hash) {
		return userLiveChatRepository.findByChatIdAndHash(chatId, hash).getUID();
	}

	public UserLiveChat findByUIDAndNotFinishAndNotDiscrad(String UID) {
		return userLiveChatRepository.findByUIDAndNotFinishAndNotDiscrad(UID);
	}
	
	public Map<String,UserLiveChat> findByStauts(String status){
		List<UserLiveChat> userLiveChats = userLiveChatRepository.findByStatus(status);
		Map<String,UserLiveChat> userLiveChatsMap = new HashMap<>();
		
		for(UserLiveChat userLiveChat:userLiveChats){
			userLiveChatsMap.put(userLiveChat.getUID(),userLiveChat);
		}
		return userLiveChatsMap;
	}
	
	public UserLiveChat findLeaveMsgUserByUIDAndState(String UID, String state) {
		return userLiveChatRepository.findLeaveMsgUserByUIDAndState(UID, state);
	}

	public void updateLeaveMsgState(Long chatId, String state) {
		userLiveChatRepository.updateLeaveMsgState(chatId, state);
	}

	public Integer updateLeaveMessageState(String UID, String state) {
		return userLiveChatRepository.updateLeaveMessageState(UID, state);
	}
}