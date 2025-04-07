/*********************************************************************
* Copyright (c) 03.04.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.mp3.discogs.load.predicate;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Discogs;
import jakarta.persistence.EntityManager;

public class CacheFilter implements Predicate<Discogs> {
	private static final Logger LOG = LoggerFactory.getLogger(CacheFilter.class);
	private LoadingCache<String, String> cache;
	
	public CacheFilter() {
		cache = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
			@Override
			public String load(String key) throws Exception {
				return key.toLowerCase();
			}
		});
		
		try(EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			List<?> l = em.createNativeQuery("SELECT DISTINCT displayArtist FROM Release ORDER BY 1").getResultList();
			
			for (Object o : l) {
				cache.put((String) o, ((String) o).toLowerCase());
			}
			
			if (LOG.isInfoEnabled()) {
				LOG.info("Preloaded {} bands", String.format("%,d", l.size()));
			}
			
			l = em.createNativeQuery("SELECT DISTINCT title FROM Release ORDER BY 1").getResultList();
			
			for (Object o : l) {
				cache.put((String) o, ((String) o).toLowerCase());
			}
			
			if (LOG.isInfoEnabled()) {
				LOG.info("Preloaded {} titles", String.format("%,d", l.size()));
			}
		}
	}
	
	@Override
	public boolean test(Discogs d) {
		boolean bandPresent = cache.getIfPresent(d.getDisplayArtist()) != null; 
		boolean titlePresent = cache.getIfPresent(d.getTitle()) != null;
		
		if (!bandPresent) {
			cache.put(d.getDisplayArtist(), d.getDisplayArtist().toLowerCase());
		}
		
		if (!titlePresent) {
			cache.put(d.getTitle(), d.getTitle().toLowerCase());
		}
		
		return !(bandPresent && titlePresent);  
	}
}
