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

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import de.tgmz.discogs.domain.Artist;
import jakarta.persistence.EntityManager;

public class ArtistPersistable implements IPersistable<Artist> {
	private Predicate<Artist> filter;
	private Map<Long, Artist> cache;
	
	public ArtistPersistable() {
		this(x -> true);
	}

	public ArtistPersistable(Predicate<Artist> filter) {
		this.filter = filter;
		this.cache = new TreeMap<>();
	}

	@Override
	public int save(EntityManager em, Artist draft) {
		if (filter.test(draft)) {
			Artist a0 = em.find(Artist.class, draft.getId());
			
			if (a0 == null) {
				a0 = draft;
			} else {
				a0.setAliases(draft.getAliases());
				a0.setDataQuality(draft.getDataQuality());
				a0.setGroups(draft.getGroups());
				a0.setMembers(draft.getMembers());
				a0.setName(draft.getName());
				a0.setRealName(draft.getRealName());
				a0.setVariations(draft.getVariations());
			}
			
			a0.getAliases().replaceAll(a -> getOrCreate(em, a));
			a0.getGroups().replaceAll(a -> getOrCreate(em, a));
			a0.getMembers().replaceAll(a -> getOrCreate(em, a));
			
			em.merge(a0);
			
			cache.clear();
			
			return 1;
		} else {
			return 0;
		}
	}

	private Artist findOrCreate(EntityManager em, Artist draft) {
		Artist a0 = em.find(Artist.class, draft.getId());
		
		if (a0  == null) {
			a0 = draft;
		}
		
		return a0;
	}
	
	private Artist getOrCreate(EntityManager em, Artist draft) {
		return cache.computeIfAbsent(draft.getId(), a -> findOrCreate(em, draft));
	}
}
