package com.bcs.core.taishin.circle.db.entity;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Repository
public class CircleEntityManagerControl {
    private static final String INIT_FLAG = "INIT_FLAG";

    @PersistenceContext
    private EntityManager entityManager;

    // 注意！此數值必須和 spring-base.xml 的 hibernate.jdbc.batch_size 值相同
    public static int batchSize = 50;

    private static AtomicBoolean isRun = new AtomicBoolean(false);

    private ConcurrentLinkedQueue<Object> queueAdd = new ConcurrentLinkedQueue<Object>();

    public CircleEntityManagerControl() {

    }

    public void flushAll() {
        if (isRun.compareAndSet(false, true)) {
            try {
                ApplicationContextProvider.getApplicationContext().getBean(CircleEntityManagerControl.class)
                        .persistFlush();
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
            }
            isRun.set(false);
        }
    }

    public void persist(Object obj) {
        queueAdd.add(obj);
        log.debug("persistAdd:" + obj);
        if (queueAdd.size() >= batchSize) {
            if (isRun.compareAndSet(false, true)) {
                try {
                    ApplicationContextProvider.getApplicationContext().getBean(CircleEntityManagerControl.class)
                            .persistFlush();
                } catch (Exception e) {
                    log.error(ErrorRecord.recordError(e));
                }
                isRun.set(false);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, timeout = 3000)
    public void persistFlush() {
//		log.info("EntityManagerControl persistFlush execute");

        try {
            synchronized (INIT_FLAG) {
                boolean isPersistAdd = false;
                log.debug("CircleEntityManagerControl persistFlush execute:" + queueAdd.size());

                int i = 0;
                while (!queueAdd.isEmpty()) {

                    try {
                        Object obj = queueAdd.poll();
                        if (obj != null) {
                            entityManager.persist(obj);
                            isPersistAdd = true;
                        }
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e, false));
                        break;
                    }

                    i++;

                    if (i % batchSize == 0) {
                        if (isPersistAdd) {
                            entityManager.flush();
                            entityManager.clear();
                        }
                    }
                }

                if (isPersistAdd) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }

        isRun.set(false);
        log.debug("CircleEntityManagerControl persistFlush end");
    }


    @Transactional(rollbackFor = Exception.class, timeout = 3000)
    public void merge(List<Object> list) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        int i = 0;
        for (Object obj : list) {
            entityManager.merge(obj);
            i++;
            if (i % batchSize == 0) {

                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
    }
}
