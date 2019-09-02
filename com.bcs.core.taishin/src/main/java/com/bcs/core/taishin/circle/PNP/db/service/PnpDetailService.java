package com.bcs.core.taishin.circle.PNP.db.service;
//
//import java.util.List;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
//import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailRepository;
//
//
//@Service
//public class PnpDetailService {
//
//	/** Logger */
//	private static Logger logger = Logger.getLogger(PnpDetailService.class);
//	@Autowired
//	private PnpDetailRepository pnpDetailRepository;
//
//	public List<PnpDetail> findByPnpMainId(Long pnpMainId){
//	    return pnpDetailRepository.findByPnpMainId(pnpMainId);
//	}
//	
//	public List<PnpDetail> findByPnpMainId(Long pnpMainId, int page, int size){
//	    Pageable pageable = new PageRequest(page, size);
//        return pnpDetailRepository.findByPnpMainId(pnpMainId, pageable).getContent();
//    }
//}
