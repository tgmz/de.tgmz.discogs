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
import de.tgmz.discogs.domain.Label;
import jakarta.persistence.EntityManager;

public class LabelFactory {
	private LoadingCache<Label, Label> labelCache;
	
	public LabelFactory() {
		super();
		
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			labelCache = Caffeine.newBuilder().build(new CacheLoader<Label, Label>() {
				@Override
				public Label load(Label key) {
					return em.find(Label.class, key.getId());
				}
			});
		}
	}
	
	public Label get(Label draft) {
		return labelCache.get(draft, a -> draft);
	}
}
