package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpContentTemplateMsgAction;

public interface PnpContentTemplateMsgActionRepository extends EntityRepository<PnpContentTemplateMsgAction, String>{

	@Query("select  b from PnpContentTemplateMsgAction b  where b.templateId = ?1 and b.status <> 'DELETE'  ")
	public List<PnpContentTemplateMsgAction> findNotDeletedTemplateId(String templateId);
}
