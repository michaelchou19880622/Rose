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
}
