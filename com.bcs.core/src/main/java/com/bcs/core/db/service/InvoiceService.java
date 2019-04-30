package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Invoice;
import com.bcs.core.db.repository.InvoiceRepository;
import com.bcs.core.invoice.service.InvoiceStatus;
import com.bcs.core.utils.ObjectUtil;

@Service
public class InvoiceService {
	
	@Autowired
	private InvoiceRepository invoiceRepository;
	
    /** Logger */
    private static Logger logger = Logger.getLogger(InvoiceService.class);

    public void save(Invoice invoice){
        invoiceRepository.save(invoice);
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public List<Invoice> findByCampaignId(String campaignId) {
        logger.debug("campaignId:" + campaignId);
        
        return invoiceRepository.findByCampaignId(campaignId);
    }
    
    public String generateInvoiceId() {
        String invoiceId = UUID.randomUUID().toString().toLowerCase();
        
        while (invoiceRepository.findOne(invoiceId) != null) {
            invoiceId = UUID.randomUUID().toString().toLowerCase();
        }
        return invoiceId;
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public List<Invoice> findNotUsedToGetPrizeByMid(String MID, String gameId) {
        logger.debug("MID" + MID);
        
        return invoiceRepository.findNotUsedToGetPrizeByMid(MID, gameId);
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public boolean checkIsExisted(String invNum, String campaignId) {
        logger.debug("invNum=" + invNum + ", campaignId=" + campaignId);
        
        List<String> statusList = new ArrayList<String>();
        statusList.add(InvoiceStatus.VALID);
        statusList.add(InvoiceStatus.NOT_FOUND);
        
        List<Invoice> invoiceList = invoiceRepository.findByStatusInAndInvNumAndCampaignId(statusList, invNum, campaignId);
        logger.debug(ObjectUtil.objectToJsonStr(invoiceList));
        return CollectionUtils.isNotEmpty(invoiceList);
    }
    
    public List<Invoice> findByStatus(String status) {
        logger.debug("status" + status);
        
        return invoiceRepository.findByStatus(status);
    }
}
