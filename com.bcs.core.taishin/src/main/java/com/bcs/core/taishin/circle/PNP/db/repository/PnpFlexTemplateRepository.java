package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpFlexTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Pnp Flex Template Repository
 * @author Alan
 */
@Repository
public interface PnpFlexTemplateRepository extends JpaRepository<PnpFlexTemplate, Long>, JpaSpecificationExecutor<PnpFlexTemplate> {

}
