package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpContentTemplateMsg;

public interface PnpContentTemplateMsgRepository extends EntityRepository<PnpContentTemplateMsg, String>{

	@Query("select  b from PnpContentTemplateMsg b  where b.templateId = ?1 and b.status <> 'DELETE' and b.productSwitch = 1 ")
	public List<PnpContentTemplateMsg> findProductSwitchOnTemplateId(String templateId);
	
	@Query("select  b.templateId from PnpContentTemplateMsg b  where b.status <> 'DELETE' and b.productSwitch = 1 ")
	public List<String> findProductSwitchOnTemplateId();
	
	@Query("select  b from PnpContentTemplateMsg b  where b.templateTitle = ?1 and b.status <> 'DELETE' ")
	public List<PnpContentTemplateMsg> findByTemplateTitle(String templateTitle);
}
