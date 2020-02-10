package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMitake;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PnpDetailMitakeRepository extends EntityRepository<PnpDetailMitake, Long> {

    @Transactional(readOnly = true, timeout = 30)
    List<PnpDetailMitake> findByPnpDetailId(Long noticeDetailId);

    @Transactional(readOnly = true, timeout = 30)
    List<PnpDetailMitake> findByPnpDetailIdAndStatus(Long pnpDetailId, String status);

    @Query("select  b from PnpDetailMitake b  where b.pnpDetailId in (?1) ")
    public List<PnpDetailMitake> findByPnpDetailIds(List<Long> pnpDetailId);

    @Query(value = "select count(pnpDetailId) from PnpDetailMitake b  where b.pnpMainId = ?1 and b.status in (?2) ")
    public Long countByPnpMainIdAndStatus(Long pnpMainId, List<String> status);

    @Query(value = "select count(pnpDetailId) from PnpDetailMitake b  where b.pnpDetailId = ?1 and b.status in (?2) ")
    public Long countByPnpDetailIdAndStatus(Long pnpDetailId, List<String> status);

    @Modifying
    @Query("update PnpDetailMitake x set x.status = ?1 , x.modifyTime = ?2 where x.pnpMainId = ?3  ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    void updateStatusByMainId(String status, Date modifyTime, Long pnpMainId);


    /**
     * Find By Bc Status For Sms Schedule
     *
     * @param bcStatus BC Status
     * @return PnpDetailMitake List
     */
    List<PnpDetailMitake> findByBcStatus(String bcStatus);

    /**
     * Find By Bc Status For Sms Schedule
     *
     * @param bcStatus BC Status
     * @return PnpDetailMitake List
     */
    List<PnpDetailMitake> findTop100ByBcStatusAndSmsStatusIsNullOrderByCreateTimeAsc(String bcStatus);

    /**
     * Find By Pnp Status For Sms Schedule
     *
     * @param pnpStatus Pnp Status
     * @return PnpDetailMitake List
     */
    List<PnpDetailMitake> findTop100ByPnpStatusAndSmsStatusIsNullOrderByCreateTimeAsc(String pnpStatus);

    /**
     * find By PnpStatus And PnpDeliveryExpireTime Less And Equals Compare Time
     *
     * @param pnpStatus   pnp Status
     * @param compareTime compare Time
     * @return Detail List
     */
    List<PnpDetailMitake> findByPnpStatusAndPnpDeliveryExpireTimeBefore(String pnpStatus, Date compareTime);

    /**
     * find By PnpStatus And PnpDeliveryExpireTime Less And Equals Compare Time
     *
     * @param pnpStatus   pnp Status
     * @param compareTime compare Time
     * @return Detail List
     */
    List<PnpDetailMitake> findTop100ByPnpStatusAndPnpDeliveryExpireTimeBeforeOrderByCreateTimeAsc(String pnpStatus, Date compareTime);

}
