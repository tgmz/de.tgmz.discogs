/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

public abstract class AbstractDefaultPersistable<T> implements IPersistable<T> {
	static final Logger LOG = LoggerFactory.getLogger(AbstractDefaultPersistable.class);
	
	private List<T> cache = new LinkedList<>();
	
	public abstract IFactory<T> getFactory();
	public abstract Predicate<T> getFilter();
	
	@Override
	public int save(int threshold, T o) {
		if (getFilter().test(o)) {
			LOG.debug("Cache {}", o);
			
			cache.add(o);
		} else {
			LOG.debug("Ignore {}", o);
		}
		
		if (cache.size() == threshold) {
			flush();
			
			cache.clear();
			
			return threshold;
		}
		
		return 0;
	}
	@Override
	public int flush() {
		try (EntityManager em =  DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			em.getTransaction().begin();

			for (T t : cache) {
				LOG.debug("Save {}", t);
				
				try {
					em.merge(getFactory().get(em, t));
				} catch (PersistenceException e) {
					if ("Duplicate row was found and `ASSERT` was specified".equals(e.getMessage())) {
						// Existing release with subTracks yields "Duplicate row was found and `ASSERT` was specified"
						// so we must remove it first
						LOG.warn("Unable to merge entity {}, removing and inserting it", t);
				
						// Happens only for releases
						@SuppressWarnings("unchecked")
						T r0 = (T) em.find(t.getClass(), ((Discogs) t).getId());
						em.remove(r0);
					
						LOG.debug("Save {}", t);
						
						em.persist(t);
					} else {
						LOG.error("Unable to merge release {}", ((Discogs) t).getId(), e);
						
						em.getTransaction().rollback();
						
						throw e;
					}
				}
			}
			
			em.getTransaction().commit();
		}
		
		return cache.size();
	}
}
