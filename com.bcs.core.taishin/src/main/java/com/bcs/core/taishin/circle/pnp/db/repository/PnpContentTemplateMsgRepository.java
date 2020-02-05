package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpContentTemplateMsg;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PnpContentTemplateMsgRepository extends EntityRepository<PnpContentTemplateMsg, String> {

    @Query("select  b from PnpContentTemplateMsg b  where b.templateId = ?1 and b.status <> 'DELETE' and b.productSwitch = 1 ")
    public List<PnpContentTemplateMsg> findProductSwitchOnTemplateId(String templateId);

    @Query("select  b.templateId from PnpContentTemplateMsg b  where b.status <> 'DELETE' and b.productSwitch = 1 ")
    public List<String> findProductSwitchOnTemplateId();

    @Query("select  b from PnpContentTemplateMsg b  where b.templateTitle = ?1 and b.status <> 'DELETE' ")
    public List<PnpContentTemplateMsg> findByTemplateTitle(String templateTitle);

    //20190708
    @Query("select  b from PnpContentTemplateMsg b  where b.templateId = ?1 and b.status <> 'DELETE' ")
    public List<PnpContentTemplateMsg> findByTemplateId(String templateId);

    @Query("select  b from PnpContentTemplateMsg b  where b.templateParentId = ?1 and b.status <> 'DELETE' ")
    public List<PnpContentTemplateMsg> findByParentTemplateId(String parentTemplateId);
    //20190708
}
