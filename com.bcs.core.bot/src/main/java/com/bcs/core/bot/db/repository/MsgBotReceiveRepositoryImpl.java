package com.bcs.core.bot.db.repository;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.repository.EntityManagerControl;
import com.bcs.core.db.service.EntityManagerProviderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author ???
 */
@Slf4j
public class MsgBotReceiveRepositoryImpl {

    @Autowired
    private EntityManagerControl entityManagerControl;

    @Resource
    private EntityManagerProviderService providerService;

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
    @Transactional(rollbackFor = Exception.class)
    public void updatePnpDetailStatus(String detailTable, String detailId) {
        try {
            String queryString = "UPDATE " + detailTable +
                    " SET PNP_STATUS='PNP_COMPLETE'," +
                    " SEND_TIME=:NOW," +
                    " MODIFY_TIME=:NOW," +
                    " PNP_DELIVERY_TIME=:NOW" +
                    " WHERE PNP_DETAIL_ID=" + detailId;
            log.info("queryString:" + queryString);
            int updateNum = providerService.getEntityManager().createNativeQuery(queryString)
                    .setParameter("NOW", new Date())
                    .executeUpdate();
            log.info("Update Status Return Int : {}, {}", updateNum, detailId);
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
    }
}
