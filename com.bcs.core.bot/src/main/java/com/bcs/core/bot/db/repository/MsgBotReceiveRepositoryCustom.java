package com.bcs.core.bot.db.repository;

import java.util.List;

import com.bcs.core.bot.db.entity.MsgBotReceive;

public interface MsgBotReceiveRepositoryCustom{
	
	public void bulkPersist(List<MsgBotReceive> msgReceives);
	
	public void bulkPersist(MsgBotReceive msgReceive);
}
