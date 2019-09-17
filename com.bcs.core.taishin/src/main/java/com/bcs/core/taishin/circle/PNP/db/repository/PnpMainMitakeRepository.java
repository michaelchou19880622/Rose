package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PnpMainMitakeRepository extends EntityRepository<PnpMainMitake, Long> {

    @Transactional(readOnly = true, timeout = 30)
    List<PnpMainMitake> findByStatus(String status);

    @Modifying
    @Query("update PnpMainMitake x set x.status = ?1 , x.modifyTime = ?2 where x.pnpMainId = ?3 ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    int updatePnpMainMitakeStatus(String status, Date modifyTime, Long pnpMainId);

    @Modifying
    @Query("update PnpMainMitake x set x.status = ?1 , x.procApName = ?2 , x.modifyTime = ?3  where x.pnpMainId in (?4)  ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    List<PnpMainMitake> updateStatusAndProcApName(String status, String procApName, Date modifyTime, List<Long> pnpMainId);


}
