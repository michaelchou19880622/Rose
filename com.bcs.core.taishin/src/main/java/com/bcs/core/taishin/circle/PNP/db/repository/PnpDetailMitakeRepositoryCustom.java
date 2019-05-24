package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;

public interface PnpDetailMitakeRepositoryCustom{
	public void batchInsertPnpDetailMitake(final List<PnpDetailMitake> list);
}
