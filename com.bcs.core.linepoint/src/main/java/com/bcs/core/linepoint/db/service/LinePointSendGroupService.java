package com.bcs.core.linepoint.db.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;

import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.linepoint.db.entity.LinePointSendGroup;
import com.bcs.core.linepoint.db.repository.LinePointSendGroupRepository;
import com.bcs.core.linepoint.enums.RICH_MENU_DEFAULT_SEND_GROUP;
import com.bcs.core.utils.ObjectUtil;

@Service
public class LinePointSendGroupService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointSendGroupService.class);
	
	public static final int pageSize = 1000; //PageSize Limit
	
	@Autowired
	private LinePointSendGroupRepository sendGroupRepository;
	@Autowired
	private LineUserService lineUserService;

	public List<LinePointSendGroup> findAll(){
		return sendGroupRepository.findAll();
	}
	
	public List<LinePointSendGroup> findByGroupType(String groupType){
		return sendGroupRepository.findByGroupType(groupType);
	}

	public List<LinePointSendGroup> findByRichMenuGroupId(Long richMenuGroupId){
		return sendGroupRepository.findByRichMenuGroupId(richMenuGroupId);
	}
	
	
	/**
	 * Create DefaultGroup
	 * @return Map<Long, SendGroup>
	 */
	public Map<Long, LinePointSendGroup> generateDefaultGroupMap(){
		List<LinePointSendGroup> groups = generateDefaultGroup();
		Map<Long, LinePointSendGroup> result = new LinkedHashMap<Long, LinePointSendGroup>(); 
		for(LinePointSendGroup group : groups){
			result.put(group.getGroupId(), group);
		}
		return result;
	}
	
	/**
	 * Create DefaultGroup
	 * @return List<SendGroup>
	 */
	public List<LinePointSendGroup> generateDefaultGroup(){
		List<LinePointSendGroup> groups = new ArrayList<LinePointSendGroup>();
		
		for(RICH_MENU_DEFAULT_SEND_GROUP group : RICH_MENU_DEFAULT_SEND_GROUP.values()){
			if(group.isShow()){
				groups.add(group.defaultGroup());
			}
		}
		
		return groups;
	}
	
	/**
	 * Count Default GroupSize
	 * @param groupId
	 * @return Long
	 */
	public Long countDefaultGroupSize(Long groupId){
		if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.ALL_USER.getGroupId())){
			Long result = lineUserService.countByStatus(LineUser.STATUS_BINDED);
			result += lineUserService.countByStatus(LineUser.STATUS_UNBIND);
			return result;
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.BINDED_USER.getGroupId())){
			return lineUserService.countByStatus(LineUser.STATUS_BINDED);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.UNBIND_USER.getGroupId())){
			return lineUserService.countByStatus(LineUser.STATUS_UNBIND);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.BLOCK_USER.getGroupId())){
			return lineUserService.countByStatus(LineUser.STATUS_BLOCK);
		}
		
		return null;
	}

	public Long countDefaultGroupSize(Long groupId, String start, String end){

		if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.ALL_USER.getGroupId())){
			Long result = lineUserService.countByStatus(LineUser.STATUS_BINDED, start, end);
			result += lineUserService.countByStatus(LineUser.STATUS_UNBIND, start, end);
			return result;
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.BINDED_USER.getGroupId())){
			return lineUserService.countByStatus(LineUser.STATUS_BINDED, start, end);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.UNBIND_USER.getGroupId())){
			return lineUserService.countByStatus(LineUser.STATUS_UNBIND, start, end);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.BLOCK_USER.getGroupId())){
			return lineUserService.countByStatus(LineUser.STATUS_BLOCK, start, end);
		}
		
		return null;
	}

	/**
	 * Query Default Group
	 * @param groupId
	 * @return
	 */
	public List<String> queryDefaultGroup(Long groupId, int page){
		return this.queryDefaultGroup(groupId, page, pageSize);
	}

	/**
	 * Query Default Group
	 * @param groupId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<String> queryDefaultGroup(Long groupId, int page, int pageSize){
		Page<String> resultPage = null;
		if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.ALL_USER.getGroupId())){
			resultPage = lineUserService.findMIDAllActive(page, pageSize);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.BINDED_USER.getGroupId())){
			resultPage = lineUserService.findMIDByStatus(LineUser.STATUS_BINDED, page, pageSize);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.UNBIND_USER.getGroupId())){
			resultPage =lineUserService.findMIDByStatus(LineUser.STATUS_UNBIND, page, pageSize);
		}
		
		if(resultPage != null){
			return resultPage.getContent();
		}
		
		return null;
	}
	
	public Boolean checkMidExistDefaultGroup(Long groupId, String mid){

		if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.ALL_USER.getGroupId())){
			return lineUserService.checkMIDAllActive(mid);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.BINDED_USER.getGroupId())){
			return  lineUserService.checkMIDByStatus(LineUser.STATUS_BINDED, mid);
		}
		else if(groupId.equals(RICH_MENU_DEFAULT_SEND_GROUP.UNBIND_USER.getGroupId())){
			return lineUserService.checkMIDByStatus(LineUser.STATUS_UNBIND, mid);
		}
		
		return false;
	}
	
	public Map<Long, LinePointSendGroup> findAllMap(){
		List<LinePointSendGroup> groups = sendGroupRepository.findAll();
		Map<Long, LinePointSendGroup> result = new HashMap<Long, LinePointSendGroup>();
		
		for(LinePointSendGroup group : groups){
			result.put(group.getGroupId(), group);
		}
		
		return result;
	}
	
	/**
	 * FindGroupTitleMap include Default Group
	 * @return
	 */
	public Map<Long, String> findGroupTitleMap(){
		List<Object[]> groups = sendGroupRepository.findAllGroupIdAndGroupTitle();
		logger.debug("findGroupTitleMap:" + ObjectUtil.objectToJsonStr(groups));
		Map<Long, String> result = new LinkedHashMap<Long, String>();
		
		List<LinePointSendGroup> defaults = generateDefaultGroup();
		for(LinePointSendGroup group : defaults){
			result.put(group.getGroupId(), group.getGroupTitle());
		}
		
		for(Object[] group : groups){
			BigInteger groupId = (BigInteger) group[0];
			String groupTitle= (String) group[1];
			result.put(groupId.longValue(), groupTitle);
		}
		
		return result;
	}
	
	/**
	 * FindOne
	 * @param groupId
	 * @return SendGroup
	 */
	public LinePointSendGroup findOne(Long groupId){
		if(groupId != null && groupId > 0){
			return sendGroupRepository.findOne(groupId);
		}
		else{
			Map<Long, LinePointSendGroup> map = generateDefaultGroupMap();
			return map.get(groupId);
		}
	}
	
	/**
	 * Find Group TitleByGroupId
	 * @param groupId
	 * @return String Title
	 */
	public String findGroupTitleByGroupId(Long groupId) {
		return sendGroupRepository.findGroupTitleByGroupId(groupId);
	}
	
	public void save(LinePointSendGroup sendGroup){
		sendGroupRepository.save(sendGroup);
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void delete(Long groupId) throws BcsNoticeException{
		logger.debug("delete:" + groupId);
		if(groupId < 0){
			throw new BcsNoticeException("預設群組無法刪除");
		}
		
		LinePointSendGroup sendGroup = sendGroupRepository.findOne(groupId);
		
		sendGroupRepository.delete(sendGroup);
	}
}
