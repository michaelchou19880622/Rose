package com.bcs.core.db.service;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Alan
 * @apiNote Can use @Resource inject bean
 */
@Service("entityManagerProviderService")
public class EntityManagerProviderServiceImpl implements EntityManagerProviderService {

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Get Entity Manager Instance
     *
     * @return Entity Manager Instance
     */
    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }
}
