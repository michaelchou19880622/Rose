package com.bcs.core.bot.db.repository;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.repository.EntityManagerControl;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ???
 */
@Slf4j
public class MsgBotReceiveRepositoryImpl {

    @Autowired
    private EntityManagerControl entityManagerControl;

    @PersistenceContext
    private EntityManager entityManager;

    public void bulkPersist(List<MsgBotReceive> msgReceives) {
        if (CollectionUtils.isEmpty(msgReceives)) {
            return;
        }
        for (MsgBotReceive msgReceive : msgReceives) {
            entityManagerControl.persist(msgReceive);
        }
    }

    public void bulkPersist(MsgBotReceive msgReceive) {
        entityManagerControl.persist(msgReceive);
    }

    /**
     * 更新PNP Detail Table 狀態
     * 狀態更新為 PNP_COMPLETE並更新接收時間
     *
     * @param detailTable Detail Table
     * @param detailId    Detail Id
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updatePnpDetailStatus(String detailTable, String detailId) {
        String now = DataUtils.formatDateToString(new Date(),"yyyy-MM-dd HH:mm:ss.SSS");
        String queryString = String.format(" update %s set " +
                        " status = '%s'," +
                        " pnp_status = '%s'," +
                        " send_time = convert(datetime, '%s', 121)," +
                        " modify_time = convert(datetime, '%s', 121)," +
                        " pnp_delivery_time = convert(datetime, '%s', 121) " +
                        " where pnp_detail_id = '%s';",
                detailTable, "PNP_COMPLETE", "PNP_COMPLETE", now, now, now, detailId);
        log.info("queryString:" + queryString);
        int updateNum = entityManager.createNativeQuery(queryString).executeUpdate();
        log.info("Update Status Return Int : " + updateNum);
    }

    /**
     * 更新PNP Main Table 狀態
     * 狀態更新為 PNP_COMPLETE
     *
     * @param mainTable Detail Table
     * @param mainId    Detail Id
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updatePnpMainStatus(String mainTable, String mainId) {
        String now = DataUtils.formatDateToString(new Date(),"yyyy-MM-dd HH:mm:ss.SSS");
        String queryString = String.format(" update %s set " +
                        " status = '%s'," +
                        " send_time = convert(datetime, '%s', 121)," +
                        " modify_time = convert(datetime, '%s', 121)," +
                        " where pnp_main_id = '%s';",
                mainTable, "PNP_COMPLETE", now, now, mainId);
        log.info("queryString:" + queryString);
        int updateNum = entityManager.createNativeQuery(queryString).executeUpdate();
        log.info("Update Status Return Int : " + updateNum);
    }


}
