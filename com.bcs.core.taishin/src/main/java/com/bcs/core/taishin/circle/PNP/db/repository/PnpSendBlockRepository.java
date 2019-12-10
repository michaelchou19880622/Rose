package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpSendBlock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PnpSendBlockRepository extends JpaRepository<PnpSendBlock, Long>{
  List<PnpSendBlock> findByPhone(String phone);
  List<PnpSendBlock> findByUid(String uid);
}