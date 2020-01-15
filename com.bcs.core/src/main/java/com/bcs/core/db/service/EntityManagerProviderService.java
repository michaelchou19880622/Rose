package com.bcs.core.db.service;

import javax.persistence.EntityManager;

/**
 * @author Alan
 */
public interface EntityManagerProviderService {
    /**
     * Get Entity Manager Instance
     *
     * @return Entity Manager Instance
     */
    EntityManager getEntityManager();
}
