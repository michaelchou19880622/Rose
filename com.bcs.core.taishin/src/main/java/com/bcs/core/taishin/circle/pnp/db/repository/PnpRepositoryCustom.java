package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The interface Pnp repository custom.
 *
 * @author Alan
 */
public interface PnpRepositoryCustom {
    /**
     * Find all bc to pnp detail list.
     *
     * @param type     the type
     * @param stage    the stage
     * @param bcStatus the bc status
     * @return the list
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    List<PnpDetail> findAllBcToPnpDetail(PnpFtpSourceEnum type, PnpStageEnum stage, PnpStatusEnum bcStatus);

    /**
     * Find all detail list.
     *
     * @param mainId the main id
     * @param type   the type
     * @return the list
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    List<PnpDetail> findAllDetail(Long mainId, PnpFtpSourceEnum type);

    /**
     * Find all main list.
     *
     * @param procApName the proc ap name
     * @param type       the type
     * @return the list
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    List<PnpMain> findAllMain(String procApName, PnpFtpSourceEnum type);

    /**
     * Find main by id list.
     *
     * @param type   the type
     * @param mainId the main id
     * @return the list
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    List<PnpMain> findMainById(PnpFtpSourceEnum type, Long mainId);


    /**
     * Count by status list int.
     *
     * @param statusList the status list
     * @param type       the type
     * @param mainId     the main id
     * @return the int
     */
    @Transactional(rollbackFor = Exception.class)
    int countByStatusList(List<String> statusList, PnpFtpSourceEnum type, long mainId);


    /**
     * Update main to complete int.
     *
     * @param mainId the main id
     * @param type   the type
     * @param status the status
     * @return the int
     */
    @Transactional(rollbackFor = Exception.class)
    int updateMainToComplete(long mainId, PnpFtpSourceEnum type, PnpStatusEnum status);
}
