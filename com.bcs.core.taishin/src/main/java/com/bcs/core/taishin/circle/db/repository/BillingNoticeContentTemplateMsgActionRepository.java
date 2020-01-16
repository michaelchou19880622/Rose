package com.bcs.core.taishin.circle.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillingNoticeContentTemplateMsgActionRepository extends EntityRepository<BillingNoticeContentTemplateMsgAction, String> {

    @Query("select  b from BillingNoticeContentTemplateMsgAction b  where b.templateId = ?1 and b.status <> 'DELETE' order by b.actionLetter ")
    public List<BillingNoticeContentTemplateMsgAction> findNotDeletedTemplateId(String templateId);
}
