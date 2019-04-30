package com.bcs.core.db.persistence;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Custom EntityRepository interface bean.
 *
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public interface EntityRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
