package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * The interface Pnp main unica repository.
 */
public interface PnpMainUnicaRepository extends EntityRepository<PnpMainUnica, Long> {

    /**
     * Find by status list.
     *
     * @param status the status
     * @return the list
     */
    @Transactional(readOnly = true, timeout = 30)
    List<PnpMainUnica> findByStatus(String status);

    /**
     * Update pnp main unica status list.
     *
     * @param status     the status
     * @param modifyTime the modify time
     * @param pnpMainId  the pnp main id
     * @return the list
     */
    @Modifying
    @Query("update PnpMainUnica x set x.status = ?1 , x.modifyTime = ?2 where x.pnpMainId = ?3 ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    int updatePnpMainUnicaStatus(String status, Date modifyTime, Long pnpMainId);

    /**
     * Update status and proc ap name list.
     *
     * @param status     the status
     * @param procApName the proc ap name
     * @param modifyTime the modify time
     * @param pnpMainId  the pnp main id
     * @return the list
     */
    @Modifying
    @Query("update PnpMainUnica x set x.status = ?1 , x.procApName = ?2 , x.modifyTime = ?3  where x.pnpMainId in (?4)  ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    List<PnpMainUnica> updateStatusAndProcApName(String status, String procApName, Date modifyTime, List<Long> pnpMainId);


}
