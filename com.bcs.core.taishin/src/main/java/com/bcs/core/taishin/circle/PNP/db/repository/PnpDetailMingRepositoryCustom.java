package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;

public interface PnpDetailMingRepositoryCustom{
	public void batchInsertPnpDetailMing(final List<PnpDetailMing> list);
}
