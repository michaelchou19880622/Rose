package com.bcs.core.taishin.circle.PNP.db.repository;
//
//import java.util.List;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.bcs.core.db.persistence.EntityRepository;
//import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
//
//public interface PnpDetailRepository extends EntityRepository<PnpDetail, Long>{
//	
//    @Query(value = "select a from PnpDetail a where a.pnpMainId = ?1")
//    List<PnpDetail> findByPnpMainId(Long pnpMainId);
//    
//    @Query(value = "select a from PnpDetail a where a.pnpMainId = ?1")
//    Page<PnpDetail> findByPnpMainId(Long pnpMainId, Pageable pageable);
//    
//    @Transactional
//    @Modifying
//    @Query(value = "delete from PnpDetail a where a.pnpMainId = ?1")
//    void deleteByPnpMainId(Long pnpMainId);
//}
