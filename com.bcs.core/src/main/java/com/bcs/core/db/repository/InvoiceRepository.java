package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Invoice;
import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.invoice.service.InvoiceStatus;

@Repository
public interface InvoiceRepository extends EntityRepository<Invoice, String> {

    @Transactional(readOnly = true, timeout = 30)
    public List<Invoice> findByCampaignId(String campaignId);

    @Query(value = "SELECT * FROM BCS_INVOICE i "
            + "WHERE i.MID = ?1 "
            + "AND i.STATUS in ('" + InvoiceStatus.VALID + "', '" + InvoiceStatus.NOT_FOUND + "') "
            + "AND i.PRIZE_LIST_ID IS NULL "
            + "AND i.CAMPAIGN_ID IN (SELECT CAMPAIGN_ID FROM BCS_CAMPAIGN WHERE GAME_ID = ?2)"
            , nativeQuery = true)
    public List<Invoice> findNotUsedToGetPrizeByMid(String MID, String gameId);

    @Query("select x from Invoice x where x.status in (?1) and x.invNum = ?2 and x.campaignId = ?3") 
//    @Query("select x from Invoice x where x.status in ?1 and x.invNum = ?2 and x.campaignId = ?3") // MYSQL Difference
    List<Invoice> findByStatusInAndInvNumAndCampaignId(List<String> statusList, String invNum, String campaignId);
    
    @Transactional(readOnly = true, timeout = 30)
    public List<Invoice> findByStatus(String status);
}
