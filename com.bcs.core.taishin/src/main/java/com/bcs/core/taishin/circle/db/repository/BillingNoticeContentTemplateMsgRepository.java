package com.bcs.core.taishin.circle.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillingNoticeContentTemplateMsgRepository extends EntityRepository<BillingNoticeContentTemplateMsg, String> {

    @Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateId = ?1 and b.status <> 'DELETE' and b.productSwitch = 1 ")
    public List<BillingNoticeContentTemplateMsg> findProductSwitchOnTemplateId(String templateId);

    @Query("select  b.templateId from BillingNoticeContentTemplateMsg b  where b.status <> 'DELETE' and b.productSwitch = 1 ")
    public List<String> findProductSwitchOnTemplateId();

    @Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateTitle = ?1 and b.status <> 'DELETE' ")
    public List<BillingNoticeContentTemplateMsg> findByTemplateTitle(String templateTitle);

    @Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateTitle = ?1 and b.status <> 'DELETE'  and b.productSwitch = 1 ")
    public List<BillingNoticeContentTemplateMsg> findByTemplateTitleAndProductSwitchOn(String templateTitle);

    @Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateTitle = ?1 and b.templateLevel = 'MAIN' and b.productSwitch = 1 and b.status <> 'DELETE' ")
    public List<BillingNoticeContentTemplateMsg> findMainOnTemplateByTitle(String templateTitle);

    @Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateId = ?1 and b.status <> 'DELETE' ")
    public List<BillingNoticeContentTemplateMsg> findByTemplateId(String templateId);

    @Query("select  b from BillingNoticeContentTemplateMsg b  where b.templateParentId = ?1 and b.status <> 'DELETE' order by b.templateLetter")
    public List<BillingNoticeContentTemplateMsg> findByParentTemplateId(String parentTemplateId);
}
