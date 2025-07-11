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
import de.tgmz.discogs.domain.Artist;
import jakarta.persistence.EntityManager;

public class ArtistFactory {
	private LoadingCache<Artist, Artist> artistCache;
	
	public ArtistFactory() {
		super();
		
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			artistCache = Caffeine.newBuilder().build(new CacheLoader<Artist, Artist>() {
				@Override
				public Artist load(Artist key) {
					return em.find(Artist.class, key.getId());
				}
			});
		}
	}
	
	public Artist get(Artist draft) {
		return artistCache.get(draft, a -> draft);
	}
}
