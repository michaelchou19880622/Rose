package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpContentTemplateMsgAction;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PnpContentTemplateMsgActionRepository extends EntityRepository<PnpContentTemplateMsgAction, String> {

    @Query("select  b from PnpContentTemplateMsgAction b  where b.templateId = ?1 and b.status <> 'DELETE'  ")
    public List<PnpContentTemplateMsgAction> findNotDeletedTemplateId(String templateId);
}
