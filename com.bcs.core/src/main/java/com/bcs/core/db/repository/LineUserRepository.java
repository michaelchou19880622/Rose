package com.bcs.core.db.repository;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.persistence.EntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The interface Line user repository.
 *
 * @author ???
 */
public interface LineUserRepository extends EntityRepository<LineUser, String>, LineUserRepositoryCustom {

    /**
     * Find by mid line user.
     *
     * @param mid the mid
     * @return the line user
     */
    @Transactional(readOnly = true, timeout = 30)
    LineUser findByMid(String mid);

    /**
     * Find by mobile and birthday list.
     *
     * @param mobile   the mobile
     * @param birthday the birthday
     * @return the list
     */
    @Transactional(readOnly = true, timeout = 30)
    List<LineUser> findByMobileAndBirthday(String mobile, String birthday);

    /**
     * Find mid by mid in list.
     *
     * @param mids the mids
     * @return the list
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where x.mid in ( ?1 )")
    List<String> findMidByMidIn(List<String> mids);

    /**
     * Find LineUID List by Phone Number List
     *
     * @param phoneNumberList the phoneNumberList
     * @return Object[] 0:Phone Number 1:Line UID
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mobile , x.mid from LineUser x where x.mobile in ( ?1 )")
    List<Object[]> findMidsByMobileIn(List<String> phoneNumberList);

    /**
     * Find LineUser List by Phone Number List
     *
     * @param phoneNumberList the phoneNumberList
     * @return LineUser List
     */
    @Query
    List<LineUser> findByMobileIn(List<String> phoneNumberList);


    /**
     * Find mid by mid in and active list.
     *
     * @param mids the mids
     * @return the list
     */
    @Query("select x.mid from LineUser x where x.mid in ( ?1 ) and (x.status = 'BINDED' or x.status = 'UNBIND')")
    List<String> findMidByMidInAndActive(List<String> mids);

    /**
     * Count by status long.
     *
     * @param status the status
     * @return the long
     */
    @Transactional(readOnly = true, timeout = 30)
    Long countByStatus(String status);

    /**
     * Count by status long.
     *
     * @param status the status
     * @param start  the start
     * @param end    the end
     * @return the long
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query(value = "SELECT COUNT(DISTINCT MID) FROM BCS_LINE_USER WHERE STATUS = ?1 AND CREATE_TIME >= ?2 AND CREATE_TIME < ?3", nativeQuery = true)
    Long countByStatus(String status, String start, String end);

    /**
     * Find by status list.
     *
     * @param status the status
     * @return the list
     */
    List<LineUser> findByStatus(String status);

    /**
     * Find mid by status page.
     *
     * @param status   the status
     * @param pageable the pageable
     * @return the page
     */
    @Transactional(readOnly = true, timeout = 30)
	//Line USERID Regular Expression :「^U[0-9a-f]{32}$」    
    @Query("select CONCAT(UPPER(SUBSTRING(x.mid, 1, 1)) , SUBSTRING(x.mid , 2, 32)) from LineUser x where x.status in ( ?1 )")
    Page<String> findMIDByStatus(String status, Pageable pageable);

    /**
     * Check mid by status string.
     *
     * @param status the status
     * @param mid    the mid
     * @return the string
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where x.status in ( ?1 ) and x.mid = (?3)")
    String checkMIDByStatus(String status, String mid);

    /**
     * Check mid by status string.
     *
     * @param status the status
     * @param mid    the mid
     * @return the string
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where x.status in ( ?1, ?2 ) and x.mid = (?3)")
    String checkMIDByStatus(String status, String status2, String mid);

    /**
     * Get mid by mobile.
     *
     * @param mobile the mobile
     * @return the string
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where x.mobile in ( ?1 )")
    String getMidByMobile(String mobile);

    /**
     * Find mid all active page.
     *
     * @param pageable the pageable
     * @return the page
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where x.status = 'BINDED' or x.status = 'UNBIND'")
    Page<String> findMIDAllActive(Pageable pageable);

    /**
     * Check mid all active string.
     *
     * @param mid the mid
     * @return the string
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where (x.status = 'BINDED' or x.status = 'UNBIND') and x.mid = (?1)")
    String checkMIDAllActive(String mid);

    
    /**
     * Check mid all active and SYSADD string.
     *
     * @param mid the mid
     * @return the string
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.mid from LineUser x where (x.status = 'BINDED' or (x.status = 'UNBIND' or x.status = 'SYSADD')) and x.mid = (?1)")
    String checkMIDAllActiveAndSysAdd(String mid);
    

    /**
     * Find by create time list.
     *
     * @param start the start
     * @param end   the end
     * @return the list
     */
    @Transactional(readOnly = true, timeout = 30)
    @Query(value = "select * from BCS_LINE_USER where CREATE_TIME >= ?1 and CREATE_TIME < ?2 order by CREATE_TIME", nativeQuery = true)
    List<LineUser> findByCreateTime(String start, String end);
}
