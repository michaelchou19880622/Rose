package com.bcs.core.bot.scheduler.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.bot.scheduler.service.SchedulerService;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.GroupGenerateService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;

public class ExecuteSendMsgTask {

	/** Logger */
	private static Logger logger = Logger.getLogger(ExecuteSendMsgTask.class);

	public void executeSendMsg(Long msgId) throws Exception{
		logger.debug("executeSendMsg msgId ============ :" + msgId);

		GroupGenerateService groupGenerateService = ApplicationContextProvider.getApplicationContext().getBean(GroupGenerateService.class);
		SendGroupService sendGroupService = ApplicationContextProvider.getApplicationContext().getBean(SendGroupService.class);
		MsgMainService msgMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgMainService.class);
		MsgSendMainService msgSendMainService = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class);
		MsgDetailService msgDetailService = ApplicationContextProvider.getApplicationContext().getBean(MsgDetailService.class);

		MsgMain msgMain = msgMainService.findOne(msgId);
		if(msgMain != null){
			String groupTitle = "---";
			try{	
				// Validate GroupId
				SendGroup sendGroup = sendGroupService.findOne(msgMain.getGroupId());
				if(sendGroup == null){
						throw new BcsNoticeException("群組設定錯誤");
				}
				else{
					groupTitle = sendGroup.getGroupTitle();
				}
				
				// Validate Send Target Count
				
				Long groupId = sendGroup.getGroupId();
				// 行銷人員設定 群組
				if(groupId > 0){
					try{
						List<String> mids =  groupGenerateService.findMIDBySendGroupDetailGroupId(groupId);
						if(mids != null && mids.size() >0){
							logger.debug("executeSendMsg mids ============ :" + mids.size());
							/**
							 * Copy From MsgMain to MsgSendMain
							 */
							MsgSendMain msgSendMain = msgSendMainService.copyFromMsgMain(msgId, new Long(mids.size()), groupTitle);
							
							List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);
							
							/**
							 * Send To Test Group for Check Send
							 */
							this.sendToAdminGroup(msgSendMain, details);
							
							// Reset Message
							details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

							int pageSize = SendGroupService.pageSize;
							
							List<String> sendMids = new ArrayList<String>();
							for(String mid : mids){
								sendMids.add(mid);
								
								if(sendMids.size() % pageSize == 0){

									// Handle : Sending
									sendMsgToMids(sendMids, details, msgSendMain.getMsgSendId());
									sendMids = new ArrayList<String>();
								}
							}

							if(sendMids.size() > 0){
								// Handle : Sending Else
								sendMsgToMids(sendMids, details, msgSendMain.getMsgSendId());
							}
						}
						else{
							throw new BcsNoticeException("群組設定錯誤:查不到發送目標");
						}
					}
					catch(Exception e){
						throw e;
					}
				}
				// 預設群祖
				else{
					Long totalCount = sendGroupService.countDefaultGroupSize(groupId);
					logger.debug("countDefaultGroupSize:" + totalCount);

					/**
					 * Copy From MsgMain to MsgSendMain
					 */
					MsgSendMain msgSendMain = msgSendMainService.copyFromMsgMain(msgId, totalCount, groupTitle);
					
					List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

					logger.debug("findByMsgIdAndMsgParentType");

					/**
					 * Send To Test Group for Check Send
					 */
					this.sendToAdminGroup(msgSendMain, details);

					// Reset Message
					details = msgDetailService.findByMsgIdAndMsgParentType(msgSendMain.getMsgSendId(), MsgSendMain.THIS_PARENT_TYPE);

					int pageSize = SendGroupService.pageSize;
					if(totalCount > 80*5000){
						pageSize = 5000;
					}
					
					int page = 0;
					while(true){
						List<String> list = sendGroupService.queryDefaultGroup(groupId, page, pageSize);
						if(list != null && list.size() > 0){
							logger.debug("queryDefaultGroup:" + list.size());
							// Handle : Sending
							sendMsgToMids(list, details, msgSendMain.getMsgSendId());
						}
						else{
							break;
						}
						page++;
						if(page % 80 == 0){
							// delay 3 minutes
							Thread.sleep(3*60*1000);
						}
					}
				}
				
				// Update DELAY Status
				if(MsgMain.SENDING_MSG_TYPE_DELAY.equals(msgMain.getSendType())){
					msgMain.setStatus(MsgMain.MESSAGE_STATUS_COMPLETE);
					msgMainService.save(msgMain);
				}
				// Update IMMEDIATE Status
				else  if(MsgMain.SENDING_MSG_TYPE_IMMEDIATE.equals(msgMain.getSendType())){
					msgMain.setStatus(MsgMain.MESSAGE_STATUS_COMPLETE);
					msgMainService.save(msgMain);
				}
			}
			catch(Exception e){
				logger.error(ErrorRecord.recordError(e));

				if(MsgMain.SENDING_MSG_TYPE_DELAY.equals(msgMain.getSendType())){
					msgMain.setStatus(MsgMain.MESSAGE_STATUS_FAIL);
					msgMain.setStatusNotice(e.getMessage());
					msgMain.setModifyTime(new Date());
					msgMainService.save(msgMain);
					
					msgSendMainService.copyFromMsgMain(msgId, -1L, groupTitle, MsgMain.MESSAGE_STATUS_FAIL);
					
					// Remove Scheduler
					SchedulerService schedulerService = ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class);
					boolean status = schedulerService.deleteMsgSendSchedule(msgId);
					logger.error("Schdeuler deleteMsgSendSchedule:" + msgId + " - status - " + status);
				}
				else if(MsgMain.SENDING_MSG_TYPE_SCHEDULE.equals(msgMain.getSendType())){
					
					msgSendMainService.copyFromMsgMain(msgId, -1L, groupTitle, MsgMain.MESSAGE_STATUS_FAIL, e.getMessage());
				}
				
				throw e;
			}
		}
		else{
			logger.error("Schdeuler MsgId:" + msgId + " Missing");
			// Remove Scheduler
			SchedulerService schedulerService = ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class);
			boolean status = schedulerService.deleteMsgSendSchedule(msgId);
			logger.error("Schdeuler deleteMsgSendSchedule:" + msgId + " - status - " + status);
		}
	}
	
	/**
	 *  Send To Test Group for Check Send
	 * @param msgSendMain
	 * @param details
	 */
	private void sendToAdminGroup(MsgSendMain msgSendMain, List<MsgDetail> details){

		try{
			AdminUserService adminUserService = ApplicationContextProvider.getApplicationContext().getBean(AdminUserService.class);
			
			List<AdminUser> list = adminUserService.findByMidNotNull();
			List<String> midsTest = new ArrayList<String>();
			if(list != null && list.size() > 0){
				for(AdminUser adminUser : list){
					if(StringUtils.isNotBlank(adminUser.getMid())){
						midsTest.add(adminUser.getMid());
					}
				}
			}
			
			MsgDetail detail = new MsgDetail();
			detail.setText("***此為發送訊息後通知管理群***");
			detail.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
			
			details.add(0, detail);
			
			sendMsgToMids(midsTest, details, msgSendMain.getMsgSendId());
		}
		catch(Exception e){ }
	}
	
	public void sendMsgToMids(List<String> mids, List<MsgDetail>details, Long updateMsgId) throws Exception{

		SendingMsgService sendingMegService = ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class);
		
		List<MsgGenerator> msgGenerators = MsgGeneratorFactory.validateMessages(details);
		
		logger.info("sendMsgToMids:Mids:" + mids.size());
		sendingMegService.sendToLineAsync(msgGenerators, details, mids, API_TYPE.BOT, updateMsgId);
	}
}
