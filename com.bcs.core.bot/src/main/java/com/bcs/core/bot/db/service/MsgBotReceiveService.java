package com.bcs.core.bot.db.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.repository.MsgBotReceiveRepository;
import com.bcs.core.bot.db.repository.MsgBotReceiveRepositoryImpl;

@Service
public class MsgBotReceiveService {

	/** Logger */
	private static Logger logger = Logger.getLogger(MsgBotReceiveService.class);
	
	@Autowired
	private MsgBotReceiveRepository msgBotReceiveRepository;
	
	public void save(MsgBotReceive msgReceive){
		msgBotReceiveRepository.save(msgReceive);
	}
	
	public Page<MsgBotReceive> findAll(Pageable pageable){
		return msgBotReceiveRepository.findAll(pageable);
	}
	
	public Page<MsgBotReceive> findByUserStatus(String userStatus, Pageable pageable){
		return msgBotReceiveRepository.findByUserStatus(userStatus, pageable);
	}
	
	public void bulkPersist(List<MsgBotReceive> msgReceives){
		msgBotReceiveRepository.bulkPersist(msgReceives);
	}
	
	public void bulkPersist(MsgBotReceive msgReceive){
		msgBotReceiveRepository.bulkPersist(msgReceive);
	}
	
	public Long countReceive(String start, String end){
		return msgBotReceiveRepository.countReceive(start, end);
	}
	
	public List<Object[]> countReceiveByReferenceId(String referenceId, String start, String end){
		return msgBotReceiveRepository.countReceiveByReferenceId(referenceId, start, end);
	}
	
	public List<Object[]> countReceiveByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus){
		return msgBotReceiveRepository.countReceiveByReferenceIdAndStatus(referenceId, start, end, userStatus);
	}
	
	public List<String> findReceiveMidByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus){
		return msgBotReceiveRepository.findReceiveMidByReferenceIdAndStatus(referenceId, start, end, userStatus);
	}
	
	public Long countReceiveByType(String start, String end, String eventType){
		return msgBotReceiveRepository.countReceiveByType(start, end, eventType);
	}
	
	public List<String> findReferenceId(String start, String end){
	    return msgBotReceiveRepository.findReferenceId(start, end);
	}
	
	public List<MsgBotReceive> findByReceiveDay(String start, String end){
	    return msgBotReceiveRepository.findByReceiveDay(start, end);
	}
	
	public void updatePnpStatus(String deliveryTags) {
		logger.info("received PNP delivery : "+ deliveryTags +" updateStatu to pnp send completed!");
		try {
			String[] deliveryData = deliveryTags.split("\\;;", 5);
			String source = deliveryData[1];
			String mainId = deliveryData[2];
			String detailId = deliveryData[3];
			String hashPhone = deliveryData[4];
			
			String detailTable="";
			String mainTable="";
			switch (source) {
			/**
			 * 	//三竹來源
			 *	public static final String SOURCE_MITAKE = "1";
			 *	//互動來源
			 *	public static final String SOURCE_EVERY8D = "2";
			 *	//明宣來源
			 *	public static final String SOURCE_MING = "3";
			 *	//UNICA來源
			 *	public static final String SOURCE_UNICA = "4";
			 *
			 *  MITAKE("mitake",AbstractPnpMainEntity.SOURCE_MITAKE, "BCS_PNP_MAIN_MITAKE", "BCS_PNP_DETAIL_MITAKE"),
	         *  EVERY8D("every8d", AbstractPnpMainEntity.SOURCE_EVERY8D, "BCS_PNP_MAIN_EVERY8D", "BCS_PNP_DETAIL_EVERY8D"),
	         *  MING("ming" , AbstractPnpMainEntity.SOURCE_MING, "BCS_PNP_MAIN_MING", "BCS_PNP_DETAIL_MING"),
	         *  UNICA("unica", AbstractPnpMainEntity.SOURCE_UNICA, "BCS_PNP_MAIN_UNICA", "BCS_PNP_DETAIL_UNICA");
			 *
			 *
			 */
				case "1":
					detailTable = "BCS_PNP_DETAIL_MITAKE";
					mainTable = "BCS_PNP_MAIN_MITAKE";
					break;
				case "2":
					detailTable = "BCS_PNP_DETAIL_EVERY8D";
					mainTable = "BCS_PNP_MAIN_EVERY8D";
					break;
				case "3":
					detailTable = "BCS_PNP_DETAIL_MING";
					mainTable = "BCS_PNP_MAIN_MING";
					break;
				case "4":
					detailTable = "BCS_PNP_DETAIL_UNICA";
					mainTable = "BCS_PNP_MAIN_UNICA";
					break;
			}
			
			//Date  now = Calendar.getInstance().getTime();
			//Date now = new Date();
			
//			String sqlString = 
//					 "update " + detailTable + "  set STATUS = :newStatus  , MODIFY_TIME = :modifyTime ,PNP_DELIVERY_TIME = :deliveryTime"
//					 + " where PNP_MAIN_ID =:mainId AND PNP_DETAIL_ID =:detailId";
//					entityManager.createNativeQuery(sqlString)
//					.setParameter("mainId", mainId)
//					.setParameter("detailId", detailId)
//					.setParameter("newStatus", "COMPLETE")
//					.setParameter("modifyTime", now)
//					.setParameter("deliveryTime", now)
//					.executeUpdate();
			
			
			// webhook
			logger.info("detailTable:"+detailTable);
			logger.info("detailId:"+detailId);
			
			msgBotReceiveRepository.updateStatus(detailTable, detailId);
			
			
					

			
			
			//判斷是否所有的detail都complete，若都complete則更新main狀態		
//			String judgeCompleteSQL = "select (case when a.complateCount = b.detailCount then 'true' else 'false' end)  from "
//					+ "( select count(0) as complateCount from " + detailTable + " where PNP_MAIN_ID =:mainId and status = 'COMPLETE' ) a ,"
//					+ "( select count(0) as complateCount from " + detailTable + " where PNP_MAIN_ID =:mainId ) b ";
//			       boolean judge =  (boolean) entityManager.createNativeQuery(judgeCompleteSQL).setParameter("mainId", mainId).getSingleResult();
//					
//	        if(judge){
//	        	String updateMainSQL = "update " + mainTable + "  set STATUS = :newStatus  , MODIFY_TIME = :modifyTime "
//						 + " where PNP_MAIN_ID =:mainId  ";
//	        	entityManager.createNativeQuery(updateMainSQL)
//				.setParameter("mainId", mainId)
//				.setParameter("newStatus", "COMPLETE")
//				.setParameter("modifyTime", now)
//				.executeUpdate();
//	        }
			       
		}catch(Exception e) {
			logger.error(e);
			throw e;
		}
		
	} 
}
