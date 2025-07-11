/*********************************************************************
* Copyright (c) 11.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Genre;
import jakarta.persistence.EntityManager;

public class GenreFactory {
	private LoadingCache<String, Genre> genreCache;
	
	public GenreFactory() {
		super();
		
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			genreCache = Caffeine.newBuilder().build(new CacheLoader<String, Genre>() {
				@Override
				public Genre load(String key) {
					return em.find(Genre.class, key);
				}
			});
		}
	}
	
	public Genre get(String draft) {
		return genreCache.get(draft, g -> new Genre(draft));
	}
}
