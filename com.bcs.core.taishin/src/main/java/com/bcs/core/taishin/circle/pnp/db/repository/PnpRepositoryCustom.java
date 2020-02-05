package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * The interface Pnp repository custom.
 *
 * @author ???
 */
public interface PnpRepositoryCustom {

    /**
     * Update status list.
     *
     * @param type          the type
     * @param processApName the process ap name
     * @param stage         the stage
     * @param bcStatus      the bc status
     * @return the list
     */
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    List<? super PnpDetail> updateStatus(PnpFtpSourceEnum type, String processApName, PnpStageEnum stage, PnpStatusEnum bcStatus);

    /**
     * Find all detail list.
     *
     * @param mainIds the main ids
     * @param type    the type
     * @return the list
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    List<? super PnpDetail> findAllDetail(List<Long> mainIds, PnpFtpSourceEnum type);

    /**
     * Find all wait main list.
     *
     * @param procApName the proc ap name
     * @param mainTable  the main table
     * @param type       the type
     * @return the list
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    List<? super PnpMain> findAllWaitMain(String procApName, String mainTable, PnpFtpSourceEnum type);

    /**
     * Find main by main id pnp main.
     *
     * @param type   the type
     * @param mainId the main id
     * @return the pnp main
     */
    PnpMain findMainByMainId(PnpFtpSourceEnum type, Long mainId);

    /**
     * Find pnp detail by id list.
     *
     * @param type the type
     * @param ids  the ids
     * @return the list
     */
    List<? super PnpDetail> findPnpDetailById(PnpFtpSourceEnum type, List<BigInteger> ids);
}
