package com.bcs.core.taishin.circle.PNP.db.service;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpSendBlock;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpSendBlockRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PnpSendBlockService {
  private PnpSendBlockRepository pnpSendBlockRepository;

  @Autowired
  public PnpSendBlockService(PnpSendBlockRepository pnpSendBlockRepository) {
    this.pnpSendBlockRepository = pnpSendBlockRepository;
  }

  public PnpSendBlock block(PnpSendBlock blockUser) {
    return pnpSendBlockRepository.save(blockUser);
  }

  public void unBlock(PnpSendBlock blockUser) {
    pnpSendBlockRepository.delete(blockUser);
  }

  public List<PnpSendBlock> findByPhone(String phone) {
    return pnpSendBlockRepository.findByPhone(phone);
  }

  public List<PnpSendBlock> findByUid(String uid) {
    return pnpSendBlockRepository.findByUid(uid);
  }
}