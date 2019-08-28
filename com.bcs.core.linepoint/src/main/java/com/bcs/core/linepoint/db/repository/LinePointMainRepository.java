package com.bcs.core.linepoint.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.db.persistence.EntityRepository;

public interface LinePointMainRepository extends EntityRepository<LinePointMain, Long>{
	
	@Transactional(timeout = 30)
	public LinePointMain findBySerialId(String serialId);

	@Transactional(timeout = 30)
	public LinePointMain findByTitle(String title);
	
	@Transactional(timeout = 30)
	public List<LinePointMain> findByStatus(String status);
    
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x order by x.modifyTime desc")	
	public List<LinePointMain> findAll();
    
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where x.sendType = ?1 order by x.modifyTime desc")	
	public List<LinePointMain> findBySendType(String sendType);

    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where x.sendType = ?1 "
    + "and x.modifyTime >= ?2 and x.modifyTime <= ?3 order by x.modifyTime desc")	
	public List<LinePointMain> findBySendTypeAndDate(String sendType, Date startDate, Date endDate);
    
    // with searchText
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where "
    	+ "x.title like ('%' + ?1 + '%') or x.serialId like ('%' + ?1 + '%') order by x.modifyTime desc")	
	public List<LinePointMain> findAll(String searchText);
    
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where x.sendType = ?1 "
    	+ "and (x.title like ('%' + ?2 + '%') or x.serialId like ('%' + ?2 + '%')) order by x.modifyTime desc")	
	public List<LinePointMain> findBySendType(String sendType, String searchText);
    
    // find undone 
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where x.status <> 'COMPLETE' and x.sendType = ?1 order by x.modifyTime desc")
	public List<LinePointMain> findUndoneBySendType(String sendType);
    
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where x.status = 'IDLE' "
        + " and x.allowToSend = 1 and x.sendTimingType = 'SCHEDULE' and "
    	+ " DATEDIFF(SECOND, x.sendTimingTime, GETDATE()) < 120 and DATEDIFF(SECOND, x.sendTimingTime, GETDATE()) >= 0 ) ) ")
	public List<LinePointMain> findAllowableIdles();
    
    @Transactional(timeout = 30)
    @Query(value = "select x from LinePointMain x where "
    	+ "x.title like ('%' + ?1 + '%') or x.modifyUser like ('%' + ?2 + '%') "
    	+ "and x.modifyTime >= ?3 and x.modifyTime <= ?4 "
    	+ "order by x.modifyTime desc ")	
	public List<LinePointMain> findByTitleAndModifyUserAndDate(String title, String modifyUser, Date startDate, Date endDate);
    
    @Transactional(timeout = 30)
    @Query(value = "select count(x.id) from LinePointMain x where "
    	+ "x.title like ('%' + ?1 + '%') or x.modifyUser like ('%' + ?2 + '%') "
    	+ "and x.modifyTime >= ?3 and x.modifyTime <= ?4 ")	
	public Long findTotalCountByTitleAndModifyUserAndStartDateAndEndDate(String title, String modifyUser, Date startDate, Date endDate);
}
