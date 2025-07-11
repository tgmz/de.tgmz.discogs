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
import de.tgmz.discogs.domain.Style;
import jakarta.persistence.EntityManager;

public class StyleFactory {
	private LoadingCache<String, Style> styleCache;
	
	public StyleFactory() {
		super();
		
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			styleCache = Caffeine.newBuilder().build(new CacheLoader<String, Style>() {
				@Override
				public Style load(String key) {
					return em.find(Style.class, key);
				}
			});
		}
	}
	
	public Style get(String draft) {
		return styleCache.get(draft, g -> new Style(draft));
	}
}
