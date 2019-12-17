package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStageEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * The interface Pnp repository custom.
 *
 * @author ???
 */
public interface PnpRepositoryCustom {

    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    List<? super PnpDetail> updateStatus(PnpFtpSourceEnum type, String processApName, PnpStageEnum stage, PnpStatusEnum bcStatus);

    /**
     * Update status by stage bc list.
     *
     * @param type       the type
     * @param procApName the proc ap name
     * @param allMainIds the all main ids
     * @return the list
     */
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    List<? super PnpDetail> updateStatusByStageBc(PnpFtpSourceEnum type, String procApName, Set<Long> allMainIds);

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
