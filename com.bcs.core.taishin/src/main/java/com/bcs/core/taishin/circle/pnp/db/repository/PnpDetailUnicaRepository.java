package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailUnica;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PnpDetailUnicaRepository extends EntityRepository<PnpDetailUnica, Long> {

    @Transactional(readOnly = true, timeout = 30)
    List<PnpDetailUnica> findByPnpDetailId(Long noticeDetailId);

    @Transactional(readOnly = true, timeout = 30)
    List<PnpDetailUnica> findByPnpDetailIdAndStatus(Long pnpDetailId, String status);

    @Query("select  b from PnpDetailUnica b  where b.pnpDetailId in (?1) ")
    public List<PnpDetailUnica> findByPnpDetailIds(List<Long> pnpDetailId);

    @Query(value = "select count(pnpDetailId) from PnpDetailUnica b  where b.pnpMainId = ?1 and b.status in (?2) ")
    public Long countByPnpMainIdAndStatus(Long pnpMainId, List<String> status);

    @Query(value = "select count(pnpDetailId) from PnpDetailUnica b  where b.pnpDetailId = ?1 and b.status in (?2) ")
    public Long countByPnpDetailIdAndStatus(Long pnpDetailId, List<String> status);

    @Modifying
    @Query("update PnpDetailUnica x set x.status = ?1 , x.modifyTime = ?2 where x.pnpMainId = ?3  ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    void updateStatusByMainId(String status, Date modifyTime, Long pnpMainId);

    /**
     * Find By Bc Status For Sms Schedule
     *
     * @param bcStatus BC Status
     * @return PnpDetailMitake List
     */
    List<PnpDetailUnica> findByBcStatus(String bcStatus);

    /**
     * Find By Bc Status For Sms Schedule
     *
     * @param bcStatus BC Status
     * @return PnpDetailMitake List
     */
    List<PnpDetailUnica> findTop1ByBcStatusAndSmsStatusIsNullOrderByCreateTimeAsc(String bcStatus);

    /**
     * Find By Pnp Status For Sms Schedule
     *
     * @param pnpStatus Pnp Status
     * @return PnpDetailMitake List
     */
    List<PnpDetailUnica> findTop1ByPnpStatusAndSmsStatusIsNullOrderByCreateTimeAsc(String pnpStatus);

    /**
     * find By PnpStatus And PnpDeliveryExpireTime Less And Equals Compare Time
     *
     * @param pnpStatus   pnp Status
     * @param compareTime compare Time
     * @return Detail List
     */
    List<PnpDetailUnica> findByPnpStatusAndPnpDeliveryExpireTimeBefore(String pnpStatus, Date compareTime);

    /**
     * find By PnpStatus And PnpDeliveryExpireTime Less And Equals Compare Time
     *
     * @param pnpStatus   pnp Status
     * @param compareTime compare Time
     * @return Detail List
     */
    List<PnpDetailUnica> findTop1ByPnpStatusAndPnpDeliveryExpireTimeBeforeOrderByCreateTimeAsc(String pnpStatus, Date compareTime);

}
