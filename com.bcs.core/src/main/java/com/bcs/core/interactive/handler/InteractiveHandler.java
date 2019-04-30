package com.bcs.core.interactive.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.api.msg.MsgGeneratorExtend;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.interactive.service.InteractiveService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class InteractiveHandler {

	/** Logger */
	private static Logger logger = Logger.getLogger(InteractiveHandler.class);

	@Autowired
	private InteractiveService interactiveService;

	protected LoadingCache<String, Long> linkJoin;
	
	public InteractiveHandler(){

		linkJoin = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, Long>() {
					@Override
					public Long load(String key) throws Exception {
						return 0L;
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] InteractiveHandler cleaning up...");
		try{
			if(linkJoin != null){
				linkJoin.invalidateAll();
				linkJoin = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] InteractiveHandler destroyed.");
	}
	
	public Map<Long, List<MsgDetail>> checkJoinInteractive(String MID, String keyword) throws Exception{
		logger.debug("checkJoinInteractive");
		Long iMsgId = linkJoin.get(MID);
		if(iMsgId > 0L){
			logger.debug("Interactive Detail Create Step 2");
			Map<Long, List<MsgDetail>> result = new HashMap<Long, List<MsgDetail>>();
			List<MsgDetail> list = new ArrayList<MsgDetail>();
			List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);

			for(MsgDetail detail : details){
				if(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK.equals(detail.getMsgType())){
					
					MsgDetail set = (MsgDetail)detail.clone();
					set.setText(keyword);
					
					list.add(set);
				}
			}
			
			linkJoin.put(MID, 0L);
			
			if(list.size() > 0){
				logger.debug("Interactive Detail Create Step 2 Success");
				result.put(iMsgId, list);
				
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * @param iMsgId
	 * @return
	 */
	public List<MsgDetail> checkIsInteractive(String MID, MsgInteractiveMain main, List<MsgDetail> details){
		logger.debug("checkIsInteractive");
		if(MsgInteractiveMain.INTERACTIVE_TYPE_INTERACTIVE.equals(main.getInteractiveType())){
			logger.debug("Interactive Detail Record Step 1");
			List<MsgDetail> list = new ArrayList<MsgDetail>();
			for(MsgDetail detail : details){
				if(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK.equals(detail.getMsgType())){
					linkJoin.put(MID, main.getiMsgId());
				}
				else{
					list.add(detail);
				}
			}
			
			return list;
		}
		else{
			return details;
		}
	}
}
