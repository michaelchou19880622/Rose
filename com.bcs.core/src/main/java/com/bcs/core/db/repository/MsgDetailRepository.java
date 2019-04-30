package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgDetailRepository extends EntityRepository<MsgDetail, Long>{

    @Transactional(readOnly = true, timeout = 30)
    List<MsgDetail> findByMsgIdAndMsgParentType(Long msgId, String msgParentType);
    
    @Transactional(readOnly = true, timeout = 30)
    List<MsgDetail> findByMsgIdAndEventType(Long msgId, String eventType);
}
