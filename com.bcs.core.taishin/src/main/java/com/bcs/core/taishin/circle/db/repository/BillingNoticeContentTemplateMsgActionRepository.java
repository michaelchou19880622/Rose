package com.bcs.core.taishin.circle.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;

public interface BillingNoticeContentTemplateMsgActionRepository extends EntityRepository<BillingNoticeContentTemplateMsgAction, String>{

	@Query("select  b from BillingNoticeContentTemplateMsgAction b  where b.templateId = ?1 and b.status <> 'DELETE'  ")
	public List<BillingNoticeContentTemplateMsgAction> findNotDeletedTemplateId(String templateId);
}
