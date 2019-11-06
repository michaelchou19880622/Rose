package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * The interface Pnp repository custom.
 *
 * @author ???
 */
public interface PnpRepositoryCustom {

    /**
     * Find bc to sms id list list.
     *
     * @param detailTable the detail table
     * @return the list
     */
    List<BigInteger> findBcToSmsIdList(String detailTable);

    /**
     * Update bc to sms list.
     *
     * @param detailTable the detail table
     * @param idList      the id list
     * @return the list
     */
    List<BigInteger> updateBcToSms(String detailTable, List<BigInteger> idList);

    /**
     * Find delivery expired detail id list.
     *
     * @param detailTable the detail table
     * @return the list
     */
    List<BigInteger> findDeliveryExpiredDetailId(String detailTable);

    /**
     * Update delivery expired int.
     *
     * @param detailTable the detail table
     * @param idList      the id list
     * @return the int
     */
    int updateDeliveryExpired(String detailTable, List<BigInteger> idList);

    /**
     * Update status by stage bc list.
     *
     * @param type       the type
     * @param procApName the proc ap name
     * @param allMainIds the all main ids
     * @return the list
     */
    List<? super PnpDetail> updateStatusByStageBc(PNPFTPType type, String procApName, Set<Long> allMainIds);

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
     * Find main by main id pnp main.
     *
     * @param type   the type
     * @param mainId the main id
     * @return the pnp main
     */
    PnpMain findMainByMainId(PNPFTPType type, Long mainId);



    /**
     * Find pnp detail by id list.
     *
     * @param type the type
     * @param ids  the ids
     * @return the list
     */
    List<? super PnpDetail> findPnpDetailById(PNPFTPType type, List<BigInteger> ids);
}
