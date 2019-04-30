package com.bcs.core.taishin.circle.PNP.db.repository;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
//import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
//
//
//@Repository
//public class PnpMainRepositoryImpl  implements PnpMainRepositoryCustom {
//	@Autowired
//	private PnpMainRepository pnpMainRepository;
//
//	@Override
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//	public void increaseSendCountByPnpMainIdAndCheck(Long pnpMainId, Long increase){
//	    pnpMainRepository.increaseSendCountByPnpMainId(pnpMainId, increase);
//	    PnpMain main = pnpMainRepository.findOne(pnpMainId);
//		if(main != null && main.getTotalCount() <= main.getSendCount()){
//			main.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_FINISH);
//			pnpMainRepository.save(main);
//		}
//	}
//}
