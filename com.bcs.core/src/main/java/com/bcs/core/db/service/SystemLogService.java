package com.bcs.core.db.service;

import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.repository.SystemLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * System Log Service
 *
 * @author Alan
 */
@Slf4j
@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository systemLogRepository;

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("SysLog-Clean-Scheduled-%d")
                    .daemon(true).build()
    );

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    public void save(SystemLog systemLog) {
        systemLogRepository.save(systemLog);
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void delete(Long id) {
        systemLogRepository.delete(id);
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void delete(SystemLog systemLog) {
        systemLogRepository.delete(systemLog);
    }

    public Page<SystemLog> findAll(Pageable pageable) {
        return systemLogRepository.findAll(pageable);
    }

    public Page<SystemLog> findByTargetAndAction(String target, String action, Pageable pageable) {
        return systemLogRepository.findByTargetAndAction(target, action, pageable);
    }

    public Page<SystemLog> findByLevel(String level, Pageable pageable) {
        return systemLogRepository.findByLevel(level, pageable);
    }

    public void bulkPersist(SystemLog systemLog) {
        systemLogRepository.bulkPersist(systemLog);
    }

    public Long countByLevel(String level, String start, String end) {
        return systemLogRepository.countByLevel(level, start, end);
    }

    public Long countByTargetAndAction(String target, String action, String start, String end) {
        return systemLogRepository.countByTargetAndAction(target, action, start, end);
    }

    public Long countAll(String start, String end) {
        return systemLogRepository.countAll(start, end);
    }

    public List<SystemLog> findByTargetAndAction(String target, String action, String start, String end) {
        return systemLogRepository.findByTargetAndAction(target, action, start, end);
    }

    public List<SystemLog> findByModifyUserAndLevel(String modifyUser, String level, String start, String end) {
        return systemLogRepository.findByModifyUserAndLevel(modifyUser, level, start, end);
    }

    public void deleteLogByRange(int scheduleTime, TimeUnit unit, int deleteRangeDay) {
        scheduler.scheduleAtFixedRate(() -> {
            log.info("Search Expired System Log!!");
            List<SystemLog> expiredLogList = systemLogRepository.findTop10ByModifyTimeBefore(DateUtils.addDays(new Date(), -deleteRangeDay));
            if (CollectionUtils.isNotEmpty(expiredLogList)) {
                log.info("Start Clean!! Size:{}", expiredLogList.size());
                systemLogRepository.delete(expiredLogList);
                log.info("Clean done!!");
            } else {
                log.info("Expired System Log Not Found!!");
            }
        }, 0, scheduleTime, unit);
    }
}
