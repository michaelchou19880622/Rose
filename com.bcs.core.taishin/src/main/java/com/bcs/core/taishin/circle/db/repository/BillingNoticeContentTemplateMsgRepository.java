package com.bcs.core.taishin.circle.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;

public interface BillingNoticeContentTemplateMsgRepository extends EntityRepository<BillingNoticeContentTemplateMsg, String>{

	@Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateId = ?1 and b.status <> 'DELETE' and b.productSwitch = 1 ")
	public List<BillingNoticeContentTemplateMsg> findProductSwitchOnTemplateId(String templateId);
	
	@Query("select  b.templateId from BillingNoticeContentTemplateMsg b  where b.status <> 'DELETE' and b.productSwitch = 1 ")
	public List<String> findProductSwitchOnTemplateId();
	
	@Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateTitle = ?1 and b.status <> 'DELETE' and b.productSwitch = 1 ")
	public List<BillingNoticeContentTemplateMsg> findByTemplateTitle(String templateTitle);
}
