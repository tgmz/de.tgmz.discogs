/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.database;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * The main contract of {@code DatabaseService} is the creation of Session instances
 * and transaction management. {@code DatabaseService} is a static class with no
 * state.
 * <p>
 * 
 * A typical usage should use the following idiom:
 * 
 * <pre>
 *	EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
 *
 *	em.getTransaction().begin();
 *	//do some work
 *	em.getTransaction().commit();
 *
 *	em.close();
 * </pre>
 * 
 * For short transactions use e.g.
 * 
 * <pre>
 *	DatabaseService.getInstance().inTransaction((em) -> em.merge(o));
 * </pre>
 */
public final class DatabaseService {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseService.class);
	private static final DatabaseService INSTANCE = new DatabaseService();
	private EntityManagerFactory entityManagerFactory;

	/**
	 * Private constructor for security reasons
	 */
	private DatabaseService() {
		long start = System.nanoTime();
		
		entityManagerFactory = Persistence.createEntityManagerFactory("de.tgmz.discogs.domain");
		
		LOG.info("Startuptime database service: {} ms", (System.nanoTime() - start) / 1000000.0);
	}

	public static DatabaseService getInstance() {
		return INSTANCE;
	}

	public void inTransaction(Consumer<EntityManager> work) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			work.accept(entityManager);
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}
}
