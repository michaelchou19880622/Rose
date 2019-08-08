package com.bcs.web.ui.service;

import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.db.service.LinePointScheduledDetailService;
import com.bcs.core.exception.BcsNoticeException;

@Service
public class LinePointUIService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointUIService.class);
	@Autowired
	private LinePointMainService linePointMainService;
	@Autowired
	private LinePointDetailService linePointDetailService;	
	@Autowired
	private LinePointScheduledDetailService linePointScheduledDetailService;
	
	public LinePointMain linePointMainFindOne(Long id) {
		LinePointMain linePointMain = linePointMainService.findOne(id);
		return linePointMain;
	}
	public List<LinePointMain> linePointMainFindAll(){
		return linePointMainService.findAll();
	}
	public List<LinePointMain> linePointMainFindAll(String searchText){
		return linePointMainService.findAll(searchText);
	}	
	public List<LinePointMain> linePointMainFindManual(){
		return linePointMainService.findManual();
	}
	public List<LinePointMain> linePointMainFindManual(String searchText){
		return linePointMainService.findManual(searchText);
	}	
	public List<LinePointMain> linePointMainFindAuto(){
		return linePointMainService.findAuto();
	}
	public List<LinePointMain> linePointMainFindAuto(String searchText){
		return linePointMainService.findAuto(searchText);
	}
	public List<LinePointMain> linePointMainFindUndoneManual(){
		return linePointMainService.findUndoneManual();
	}
	public List<LinePointMain> linePointMainFindUndoneAuto(){
		return linePointMainService.findUndoneAuto();
	}
	public List<LinePointDetail> findSuccess(Long linePointMainId){
		return linePointDetailService.findSuccess(linePointMainId);
	}
	public List<LinePointDetail> findFail(Long linePointMainId){
		return linePointDetailService.findFail(linePointMainId);
	}
	public List<LinePointScheduledDetail> findScheduledDetailList(Long mainId){
		return linePointScheduledDetailService.findAll(mainId);
	}	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public LinePointMain saveLinePointMainFromUI(LinePointMain linePointMain, String adminUserAccount) throws BcsNoticeException{
		logger.info("saveFromUI:" + linePointMain);
		linePointMain.setModifyUser(adminUserAccount);
		linePointMain.setModifyTime(new Date());
		linePointMainService.save(linePointMain);
		return linePointMain;
	}
		
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteFromUI(long id, String adminUserAccount, String listType) throws BcsNoticeException {
		logger.info("deleteFromUI:" +id);		
		LinePointMain linePointMain = linePointMainService.findOne(id);
		linePointMainService.delete(linePointMain);
	}	
}
