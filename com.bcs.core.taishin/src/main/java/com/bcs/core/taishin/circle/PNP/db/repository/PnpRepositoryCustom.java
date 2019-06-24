package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;
import java.util.Set;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;

public interface PnpRepositoryCustom {

	public List<? super PnpDetail> updateStatusByStageBC(PNPFTPType type, String procApName, Set<Long> allMainIds);

	public List<? super PnpDetail> updateStatus(PNPFTPType type, String procApName, String stage);
	
	public List<? super PnpDetail> updateDelivertExpiredStatus(PNPFTPType type, String procApName, String stage);

//	public void updateStatus(String deliveryTags); 移至com.bcs.core.bot.db.repository.MsgBotReceiveRepositoryImpl

	public PnpMain findMainByMainId(PNPFTPType type, Long mainId);

	public void batchInsertPnpDetailEvery8d(final List<PnpDetailEvery8d> list);

	public void batchInsertPnpDetailMitake(final List<PnpDetailMitake> list);

	public void batchInsertPnpDetailMing(final List<PnpDetailMing> list);

	public void batchInsertPnpDetailUnica(final List<PnpDetailUnica> list);
	
	
}
