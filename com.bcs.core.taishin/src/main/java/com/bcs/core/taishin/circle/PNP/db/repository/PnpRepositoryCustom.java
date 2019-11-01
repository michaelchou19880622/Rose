package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;
import java.util.Set;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import org.springframework.stereotype.Repository;

/**
 * The interface Pnp repository custom.
 *
 * @author ???
 */
public interface PnpRepositoryCustom {

    /**
     * Update status by stage bc list.
     *
     * @param type       the type
     * @param procApName the proc ap name
     * @param allMainIds the all main ids
     * @return the list
     */
    List<? super PnpDetail> updateStatusByStageBC(PNPFTPType type, String procApName, Set<Long> allMainIds);

    /**
     * Update status list.
     *
     * @param type       the type
     * @param procApName the proc ap name
     * @param stage      the stage
     * @return the list
     */
    List<? super PnpDetail> updateStatus(PNPFTPType type, String procApName, String stage);

    /**
     * Update status list.
     *
     * @param type       the type
     * @param procApName the proc ap name
     * @param stage      the stage
     * @return the list
     */
    List<? super PnpDetail> updateStatusForSms(PNPFTPType type, String procApName, String stage);


    /**
     * Update delivert expired status list.
     *
     * @param type       the type
     * @param procApName the proc ap name
     * @param stage      the stage
     * @return the list
     */
    List<? super PnpDetail> updateDeliveryExpiredStatus(PNPFTPType type, String procApName, String stage);

    /**
     * Find main by main id pnp main.
     *
     * @param type   the type
     * @param mainId the main id
     * @return the pnp main
     */
    PnpMain findMainByMainId(PNPFTPType type, Long mainId);

    /**
     * Batch insert pnp detail every 8 d.
     *
     * @param list the list
     */
    void batchInsertPnpDetailEvery8d(final List<PnpDetailEvery8d> list);

    /**
     * Batch insert pnp detail mitake.
     *
     * @param list the list
     */
    void batchInsertPnpDetailMitake(final List<PnpDetailMitake> list);

    /**
     * Batch insert pnp detail ming.
     *
     * @param list the list
     */
    void batchInsertPnpDetailMing(final List<PnpDetailMing> list);

    /**
     * Batch insert pnp detail unica.
     *
     * @param list the list
     */
    void batchInsertPnpDetailUnica(final List<PnpDetailUnica> list);


}
