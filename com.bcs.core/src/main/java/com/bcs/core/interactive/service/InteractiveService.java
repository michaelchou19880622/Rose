package com.bcs.core.interactive.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgInteractiveDetailService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.interactive.handler.CampaignFlowHandler;
import com.bcs.core.interactive.handler.InteractiveHandler;
import com.bcs.core.interactive.handler.InteractiveOtherRoleHandler;
import com.bcs.core.interactive.model.CampaignFlowData;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.TimeRangeUtil;

@Service
public class InteractiveService {
	public static final String INTERACTIVE_SYNC = "INTERACTIVE_SYNC";
	private static final String INIT_FLAG = "INIT_FLAG";

	/** Logger */
	private static Logger logger = Logger.getLogger(InteractiveService.class);
	
	@Autowired
	private MsgInteractiveMainService msgInteractiveMainService;
	@Autowired
	private MsgInteractiveDetailService msgInteractiveDetailService;
	@Autowired
	private MsgDetailService msgDetailService;
	@Autowired
	private InteractiveHandler interactiveHandler;
    @Autowired
    private InteractiveOtherRoleHandler interactiveOtherRoleHandler;
    @Autowired
    private CampaignFlowHandler campaignFlowHandler;

	List<Long> indexSetting = new ArrayList<Long>();
	/**
	 * 歡迎回應
	 */
	List<Long>welcomeMap = new ArrayList<Long>();
	/**
	 * 歡迎回應
	 */
	Map<String, List<Long>>eventWelcomeMap = new HashMap<String, List<Long>>();
	/**
	 * 自動回應
	 */
	Map<String, List<Long>>autokeywordMap = new HashMap<String, List<Long>>();
	/**
	 * Index : 
	 * key : userStatus
	 * value : Map
	 * 					key : keyword
	 * 					value : List of MsgInteractiveMain iMsgId
	 */
	Map<Long, Map<String, Map<String, List<Long>>>> keywordMap = new HashMap<Long, Map<String, Map<String, List<Long>>>>();

	/**
	 * key : userStatus
	 * value : Map
	 * 					key : keyword
	 * 					value : List of MsgInteractiveMain iMsgId
	 */
	Map<String, Map<String, List<Long>>> blackKeywordMap = new HashMap<String, Map<String, List<Long>>>();
	
	/**
	 * Interactive Cache
	 */
	Map<Long, List<MsgDetail>> interactiveDetails = new HashMap<Long, List<MsgDetail>>();

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(INTERACTIVE_SYNC);
				if(isReSyncData){
					loadKeywordMap();
					DataSyncUtil.syncDataFinish(INTERACTIVE_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	public InteractiveService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);
		
		indexSetting = this.indexSetting();
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] InteractiveService cleaning up...");
		indexSetting.clear();
		indexSetting = null;

		welcomeMap.clear();
		welcomeMap = null;

		eventWelcomeMap.clear();
		eventWelcomeMap = null;

		autokeywordMap.clear();
		autokeywordMap = null;

		keywordMap.clear();
		keywordMap = null;

		blackKeywordMap.clear();
		blackKeywordMap = null;

		interactiveDetails.clear();
		interactiveDetails = null;
		
		System.gc();
		logger.info("[DESTROY] InteractiveService destroyed.");
	}
	
	public void loadInteractiveMap(){
		loadKeywordMap();
		DataSyncUtil.settingReSync(INTERACTIVE_SYNC);
	}
	
	/**
	 * Load Keyword Map
	 */
	public void loadKeywordMap(){
		synchronized (INIT_FLAG) {
			autokeywordMap.clear();
			welcomeMap.clear();
			eventWelcomeMap.clear();
			keywordMap.clear();
			blackKeywordMap.clear();
			
			// Interactive Cache
			interactiveDetails.clear();
			
			List<MsgInteractiveMain> blackKeywordList = msgInteractiveMainService.findByInteractiveTypeAndInteractiveStatus(MsgInteractiveMain.INTERACTIVE_TYPE_BLACK_KEYWORD, MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE);
			settingInteractiveMap(blackKeywordList, true);
			
			List<MsgInteractiveMain> keywordList = msgInteractiveMainService.findByInteractiveTypeAndInteractiveStatus(MsgInteractiveMain.INTERACTIVE_TYPE_KEYWORD, MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE);
			settingInteractiveMap(keywordList);

            List<MsgInteractiveMain> interactiveList = msgInteractiveMainService.findByInteractiveTypeAndInteractiveStatus(MsgInteractiveMain.INTERACTIVE_TYPE_INTERACTIVE, MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE);
            settingInteractiveMap(interactiveList);

            List<MsgInteractiveMain> msgCampaignList = msgInteractiveMainService.findByInteractiveTypeAndInteractiveStatus(MsgInteractiveMain.INTERACTIVE_TYPE_CAMPAIGN, MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE);
            settingInteractiveMap(msgCampaignList);
		}
	}

	private void settingInteractiveMap(List<MsgInteractiveMain> resultList){
		this.settingInteractiveMap(resultList, false);
	}
	
	/**
	 * Setting Interactive Map
	 * @param resultList
	 * @param isBlack
	 */
	private void settingInteractiveMap(List<MsgInteractiveMain> resultList, boolean isBlack){

		if(resultList != null && resultList.size() > 0){
			for(MsgInteractiveMain main : resultList){
				
				String userStatus = main.getUserStatus();
				
				// 自動回應 簡便的設計之後要改掉
				if(MsgInteractiveMain.INTERACTIVE_KEYWORD_LINE_BCS_AUTO_KEYWORD.equals(main.getMainKeyword())){
					if(MsgInteractiveMain.USER_STATUS_ALL.equals(userStatus)){
						settingKeyValue(autokeywordMap, LineUser.STATUS_UNBIND, main.getiMsgId());
						settingKeyValue(autokeywordMap, LineUser.STATUS_BINDED, main.getiMsgId());
					}
					else{
						settingKeyValue(autokeywordMap, userStatus, main.getiMsgId());
					}
				}
				// 歡迎回應 簡便的設計之後要改掉
				else if(MsgInteractiveMain.INTERACTIVE_KEYWORD_LINE_BCS_WELCOME_KEYWORD.equals(main.getMainKeyword())){
					welcomeMap.add(main.getiMsgId());
				}
				// 歡迎回應 簡便的設計之後要改掉
				else if(MsgInteractiveMain.INTERACTIVE_KEYWORD_LINE_BCS_EVENT_KEYWORD.equals(main.getMainKeyword())){
					if(MsgInteractiveMain.USER_STATUS_ALL.equals(userStatus)){
						settingKeyValue(eventWelcomeMap, LineUser.STATUS_UNBIND, main.getiMsgId());
						settingKeyValue(eventWelcomeMap, LineUser.STATUS_BINDED, main.getiMsgId());
					}
					else{
						settingKeyValue(eventWelcomeMap, userStatus, main.getiMsgId());
					}
				}
				else{
					
					List<MsgInteractiveDetail> details =msgInteractiveDetailService.findByiMsgId(main.getiMsgId());
					
					if(MsgInteractiveMain.USER_STATUS_ALL.equals(userStatus)){
						// 全部, 切分到 一般, 已串聯
						settingKeywordMap(LineUser.STATUS_UNBIND, main, details, isBlack);
						settingKeywordMap(LineUser.STATUS_BINDED, main, details, isBlack);
					}
					else{
						settingKeywordMap(userStatus, main, details, isBlack);
					}
				}
			}
		}
	}
	
	/**
	 * Setting Keyword Map
	 * @param userStatus
	 * @param main
	 * @param details
	 * @param isBlack
	 */
	private void settingKeywordMap(String userStatus, MsgInteractiveMain main, List<MsgInteractiveDetail> details, boolean isBlack){

		Map<String, List<Long>> map = null;
		if(isBlack){
			map = blackKeywordMap.get(userStatus);
			if(map == null){
				map = new HashMap<String, List<Long>>();
				blackKeywordMap.put(userStatus, map);
			}
		}
		else{
			map = getIndexSetting(main.getInteractiveIndex()).get(userStatus);
			if(map == null){
				map = new HashMap<String, List<Long>>();
				getIndexSetting(main.getInteractiveIndex()).put(userStatus, map);
			}
		}
		
		Long iMsgId = main.getiMsgId();
		this.settingKeyword(map, main.getMainKeyword(), iMsgId);
		
		for(MsgInteractiveDetail detail : details){
			this.settingKeyword(map, detail.getOtherKeyword(), iMsgId);
		}
	}
	
	private Map<String, Map<String, List<Long>>> getIndexSetting(Long index){
		Map<String, Map<String, List<Long>>> result = null;
		if(index != null){
			result = keywordMap.get(index);
		}
		else{
			index = -1L;
			result = keywordMap.get(index);
		}
		
		if(result == null){
			result = new HashMap<String, Map<String, List<Long>>>();
			keywordMap.put(index, result);
		}
		
		return result;
	}
	
	private void settingKeyword(Map<String, List<Long>> map, String keyword, Long iMsgId){
		keyword = keyword.toLowerCase();
		
		this.settingKeyValue(map, keyword, iMsgId);
	}
	
	private void settingKeyValue(Map<String, List<Long>> map, String key, Long iMsgId){
		
		List<Long> list = map.get(key);
		if(list == null){
			list = new ArrayList<Long>();
			map.put(key, list);
		}
		
		list.add(iMsgId);
	}
	
	/**
	 * Get Match keyword MsgId List
	 * @param userStatus
	 * @param keyword
	 * @return List<Long>
	 * @throws ExecutionException 
	 */
	public Map<Long, List<MsgDetail>> getMatchKeyword(String MID, String userStatus, String keyword) throws Exception{

		logger.info("getMatchKeyword:" + MID + "-userStatus:" + userStatus + "-keyword:" + keyword);
		Map<Long, List<MsgDetail>> result = interactiveHandler.checkJoinInteractive(MID, keyword);
		if(result != null && result.size() > 0){
			logger.info("checkJoinInteractive Success");
			return result;
		}
		else{
			result = new HashMap<Long, List<MsgDetail>>();
		}
		
		// MatchBlackKeyword
		Long iMsgIdBlack = getMatchBlackKeywordMsgId(userStatus, keyword);
		if(iMsgIdBlack != null){
			return null;
		}
		
		MsgInteractiveMain main = getMatchKeywordMain(userStatus, keyword, MID);
		if(main == null){
			return null;
		}
		
		List<MsgDetail> details = getMsgDetails(main.getiMsgId());
		
		/* 針對活動流程判斷 */
		if (MsgInteractiveMain.INTERACTIVE_TYPE_CAMPAIGN.equals(main.getInteractiveType())) {
		    details = campaignFlowHandler.startFlow(MID, main);
		} else {
		    details =interactiveHandler.checkIsInteractive(MID, main, details);
		}
		result.put(main.getiMsgId(), details);
		
		return result; 
	}
	
	/**
	 * Get Match BlackKeyword MsgId
	 * @param userStatus
	 * @param keyword
	 * @return
	 */
	public Long getMatchBlackKeywordMsgId(String userStatus, String keyword){

		synchronized (INIT_FLAG) {
			Map<String, List<Long>> map = blackKeywordMap.get(userStatus);
			
			if(StringUtils.isNotBlank(keyword)){
				keyword = keyword.toLowerCase();
				
				if(map != null){
					List<Long> list = map.get(keyword);
					if(list != null && list.size() > 0){
						return randomOneMsg(list);
					}
				}
			}
			
			return null;
		}
	}
	
	private List<Long> indexSetting(){
		List<Long> result = new ArrayList<Long>();
		
		for(long i = 1; i < MsgInteractiveMain.indexLimit + 1; i++){
			result.add(i);
		}
		
		result.add(-1L);
		
		return result;
	}
	
	/**
	 * Get Match Keyword Main
	 * @param userStatus
	 * @param keyword
	 * @param MID
	 * @return
	 */
	private MsgInteractiveMain getMatchKeywordMain(String userStatus, String keyword, String MID){

		synchronized (INIT_FLAG) {
			
			// Different Index
			for(Long index : indexSetting){

				Map<String, Map<String, List<Long>>> indexMap = keywordMap.get(index);
				if(indexMap != null){
					Map<String, List<Long>> map = indexMap.get(userStatus);
					
					if(StringUtils.isNotBlank(keyword)){
						keyword = keyword.toLowerCase();
						
						if(map != null){
							
							// Keyword
							List<Long> list = map.get(keyword);
							if(list != null && list.size() > 0){
								MsgInteractiveMain main = checkMatch(list, MID);
								if(main != null){
									return main;
								}
							}
						}
					}
				}
			}
	
			if(autokeywordMap != null && autokeywordMap.size() > 0 && 
				autokeywordMap.get(userStatus) != null && autokeywordMap.get(userStatus).size() > 0){
				return checkMatch(autokeywordMap.get(userStatus), MID);
			}
			
			return null;
		}
	}
	
	private MsgInteractiveMain checkMatch(List<Long> list, String MID){
		Calendar calendarNow = Calendar.getInstance();
		
		List<Long> match = new ArrayList<Long>();

		if(list != null && list.size() > 0){
			for(Long iMsgId : list){
				MsgInteractiveMain main = getMsgMain(iMsgId);
				// Check Time
				if(checkActiveTime(main, calendarNow)){
					// Check Other Role
					if(StringUtils.isNotBlank(main.getOtherRole())){
						if(interactiveOtherRoleHandler.checkMatchOtherRole(MID, main.getOtherRole())){
							match.add(iMsgId);
						}
					}
					else{
						match.add(iMsgId);
					}
				}
			}
			
			// randomOneMsg
			if(match != null && match.size() > 0){
				Long iMsgId = randomOneMsg(match);
				return getMsgMain(iMsgId);
			}
		}
		
		return null;
	}
	
	private boolean checkActiveTime(MsgInteractiveMain main, Calendar calendarNow){
		if(StringUtils.isNotBlank(main.getInteractiveTimeType())){
			if(MsgInteractiveMain.INTERACTIVE_TIME_TYPE_DAY.equals(main.getInteractiveTimeType())){
				if(main.getInteractiveStartTime() != null && main.getInteractiveEndTime() != null){

					Calendar calendarStart = Calendar.getInstance();
					calendarStart.setTime(main.getInteractiveStartTime());

					Calendar calendarEnd = Calendar.getInstance();
					calendarEnd.setTime(main.getInteractiveEndTime());
					
					return TimeRangeUtil.isDayRange(calendarNow, calendarStart, calendarEnd);
				}
			}
			else if(MsgInteractiveMain.INTERACTIVE_TIME_TYPE_RANGE.equals(main.getInteractiveTimeType())){
				if(main.getInteractiveStartTime() != null && main.getInteractiveEndTime() != null){

					if(calendarNow.getTime().compareTo(main.getInteractiveStartTime()) >= 0 && 
							calendarNow.getTime().compareTo(main.getInteractiveEndTime()) < 0){
						return true;
					}
				}
			}
		}
		else{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Random One Msg
	 * @param list
	 * @return
	 */
	private Long randomOneMsg(List<Long> list){
		logger.debug("randomOneMsg Size:" + list.size());

        int index = new Random().nextInt(list.size());
        return list.get(index);
	}
	
	/**
	 * Auto Response
	 * @return
	 */
	public MsgInteractiveMain getAutoResponse(String MID, String userStatus){
		synchronized (INIT_FLAG) {

			if(autokeywordMap != null && autokeywordMap.size() > 0 && 
				autokeywordMap.get(userStatus) != null && autokeywordMap.get(userStatus).size() > 0){
				return checkMatch(autokeywordMap.get(userStatus), MID);
			}
			
			return null;
		}
	}
	
	public boolean checkIsAutoResponse(Long iMsgId, String userStatus){

		if(autokeywordMap != null && autokeywordMap.size() > 0 && 
			autokeywordMap.get(userStatus) != null && autokeywordMap.get(userStatus).size() > 0){
			return autokeywordMap.get(userStatus).contains(iMsgId);
		}
		
		return false;
	}
	
	/**
	 * Welcome Response
	 * @return
	 */
	public Long getWelcomeResponse(){
		synchronized (INIT_FLAG) {

			if(welcomeMap != null && welcomeMap.size() > 0){
				return randomOneMsg(welcomeMap);
			}
			
			return null;
		}
	}
	
	/**
	 * Event Welcome Response
	 * @return
	 */
	public Long getEventWelcomeResponse(String userStatus){
		synchronized (INIT_FLAG) {

			if(eventWelcomeMap != null && eventWelcomeMap.size() > 0 && 
					eventWelcomeMap.get(userStatus) != null && eventWelcomeMap.get(userStatus).size() > 0){
				return randomOneMsg(eventWelcomeMap.get(userStatus));
			}
			
			return null;
		}
	}
	
	/**
	 * Get Msg Details With Cache
	 * @param iMsgId
	 * @return
	 */
	public List<MsgDetail> getMsgDetails(Long iMsgId){
		synchronized (INIT_FLAG) {
			List<MsgDetail> details = interactiveDetails.get(iMsgId);
			if(details != null){
			}
			else{
				details = msgDetailService.findByMsgIdAndMsgParentType(iMsgId, MsgInteractiveMain.THIS_PARENT_TYPE);
				interactiveDetails.put(iMsgId, details);
			}
			
			return details;
		}
	}
	
	/**
	 * Get Msg Main With Cache
	 * @param iMsgId
	 * @return
	 */
	private MsgInteractiveMain getMsgMain(Long iMsgId){
		
		return msgInteractiveMainService.findOne(iMsgId);
	}
	
	public CampaignFlowData handleCampaignFlow(String MID, Object msg) throws Exception {
        return campaignFlowHandler.handle(MID, msg);
	}
}
