package com.bcs.core.bot.db.service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.repository.MsgBotReceiveRepository;
import com.bcs.core.bot.db.repository.MsgBotReceiveRepositoryImpl;
import com.bcs.core.bot.enums.PNPFTPType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ???
 */
@Slf4j
@Service
public class MsgBotReceiveService {
    /**
     * JPA Repository
     */
    private MsgBotReceiveRepository msgBotReceiveRepository;

    /**
     * Native SQL Repository
     */
    private MsgBotReceiveRepositoryImpl msgBotReceiveRepositoryImpl;

    @Autowired
    public MsgBotReceiveService(MsgBotReceiveRepository msgBotReceiveRepository, MsgBotReceiveRepositoryImpl msgBotReceiveRepositoryImpl) {
        this.msgBotReceiveRepository = msgBotReceiveRepository;
        this.msgBotReceiveRepositoryImpl = msgBotReceiveRepositoryImpl;
    }

    public void save(MsgBotReceive msgReceive) {
        msgBotReceiveRepository.save(msgReceive);
    }

    public Page<MsgBotReceive> findAll(Pageable pageable) {
        return msgBotReceiveRepository.findAll(pageable);
    }

    public Page<MsgBotReceive> findByUserStatus(String userStatus, Pageable pageable) {
        return msgBotReceiveRepository.findByUserStatus(userStatus, pageable);
    }

    public void bulkPersist(List<MsgBotReceive> msgReceives) {
        msgBotReceiveRepositoryImpl.bulkPersist(msgReceives);
    }

    public void bulkPersist(MsgBotReceive msgReceive) {
        msgBotReceiveRepositoryImpl.bulkPersist(msgReceive);
    }

    public Long countReceive(String start, String end) {
        return msgBotReceiveRepository.countReceive(start, end);
    }

    public List<Object[]> countReceiveByReferenceId(String referenceId, String start, String end) {
        return msgBotReceiveRepository.countReceiveByReferenceId(referenceId, start, end);
    }

    public List<Object[]> countReceiveByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus) {
        return msgBotReceiveRepository.countReceiveByReferenceIdAndStatus(referenceId, start, end, userStatus);
    }

    public List<String> findReceiveMidByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus) {
        return msgBotReceiveRepository.findReceiveMidByReferenceIdAndStatus(referenceId, start, end, userStatus);
    }

    public Long countReceiveByType(String start, String end, String eventType) {
        return msgBotReceiveRepository.countReceiveByType(start, end, eventType);
    }

    public List<String> findReferenceId(String start, String end) {
        return msgBotReceiveRepository.findReferenceId(start, end);
    }

    public List<MsgBotReceive> findByReceiveDay(String start, String end) {
        return msgBotReceiveRepository.findByReceiveDay(start, end);
    }

    /**
     * Update PNP Table Status
     *
     * @param deliveryTags delivery Tags from Line
     */
    public void updatePnpStatus(String deliveryTags) {
        log.info("Received PNP Delivery Tag : " + deliveryTags + " Update Status To PNP Send Completed!!");
        try {
            String[] deliveryData = deliveryTags.split("\\;;", 5);
            String source = deliveryData[1];
            String detailId = deliveryData[3];
            String detailTable = PNPFTPType.getDetailTableNameByCode(source);
            log.info("Detail Table : {}, Detail Id: {}", detailTable, detailId);
            /* 更新Detail Table狀態 */
            msgBotReceiveRepositoryImpl.updatePnpDetailStatus(detailTable, detailId);
            log.info("Update PNP Main and Detail Status Finish!!");
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
    }
}
