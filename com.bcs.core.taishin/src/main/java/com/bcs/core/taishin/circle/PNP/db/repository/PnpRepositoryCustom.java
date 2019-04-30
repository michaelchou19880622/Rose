package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;

public interface PnpRepositoryCustom {

	public List<? super PnpDetail> findDetailByStatusForUpdateMitake(List<String> status, Long mainId);
	public List<? super PnpDetail> findDetailByStatusForUpdateEvery8d(List<String> status, Long mainId);
	public List<? super PnpDetail> findDetailByStatusForUpdateUnica(List<String> status, Long mainId);
	public List<? super PnpDetail> findDetailByStatusForUpdateMing(List<String> status, Long mainId);
	
	public PnpDetailMitake findFirstDetailByStatusForUpdateMitake(String stage ,String status);
	public PnpDetailEvery8d findFirstDetailByStatusForUpdateEvery8d(String stage ,String status);
	public PnpDetailUnica findFirstDetailByStatusForUpdateUnica(String stage ,String status);
	public PnpDetailMing findFirstDetailByStatusForUpdateMing(String stage ,String status);
	
	public PnpMainMitake findFirstMainByStatusForUpdateMitake(String stage,String status);
	public PnpMainEvery8d findFirstMainByStatusForUpdateEvery8d(String stage,String status);
	public PnpMainUnica findFirstMainByStatusForUpdateUnica(String stage,String status);
	public PnpMainMing findFirstMainByStatusForUpdateMing(String stage,String status);
	
	public PnpMainMitake findMainByMainIdMitake(Long mainId);
	public PnpMainEvery8d findMainByMainIdEvery8d(Long mainId);
	public PnpMainUnica findMainByMainIdUnica(Long mainId);
	public PnpMainMing findMainByMainIdMing(Long mainId);
	
	public List<? super PnpDetail> findDetailsWaitForPNPMitake(String stage , String status,Long mainId);
	public List<? super PnpDetail> findDetailsWaitForPNPEvery8d(String stage , String status,Long mainId);
	public List<? super PnpDetail> findDetailsWaitForPNPUnica(String stage , String status,Long mainId);
	public List<? super PnpDetail> findDetailsWaitForPNPMing(String stage , String status,Long mainId);
	
	
}
