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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.tgmz.discogs.domain.Artist;
import jakarta.persistence.EntityManager;

public class ArtistFactory implements IFactory<Artist> {
	private Map<Long, Artist> cache;
	private EntityManager em;
	
	public ArtistFactory() {
		cache = new TreeMap<>();
	}
	
	@Override
	public Artist get(EntityManager em, Artist draft) {
		this.em = em;
		
		return getWhileCached(draft);
	}
	
	private Artist getWhileCached(Artist draft) {
		Artist a0 = getOrCreate(draft);
		
		if (a0.getAliases().isEmpty()) {
			a0.setAliases(draft.getAliases());
		}
		
		if (a0.getDataQuality() == null) {
			a0.setDataQuality(draft.getDataQuality());
		}
		
		if (a0.getGroups().isEmpty()) {
			a0.setGroups(draft.getGroups());
		}
		
		if (a0.getMembers().isEmpty()) {
			a0.setMembers(draft.getMembers());
		}
		
		if (a0.getName() == null) {
			a0.setName(draft.getName());
		}
		
		if (a0.getRealName() == null) {
			a0.setRealName(draft.getRealName());
		}
		
		if (a0.getVariations().isEmpty()) {
			a0.setVariations(draft.getVariations());
		}
		
		a0.setAliases(replaceAll(a0.getAliases()));
		a0.setGroups(replaceAll(a0.getGroups()));
		a0.setMembers(replaceAll(a0.getMembers()));
		
		return a0;
	}

	private Set<Artist> replaceAll(Set<Artist> artists) {
		List<Artist> l = new LinkedList<>(artists);
		
		l.replaceAll(this::getOrCreate);
		
		return new HashSet<>(l);
	}
	
	private Artist getOrCreate(Artist draft) {
		return cache.computeIfAbsent(draft.getId(), a -> findOrCreate(em, draft));
	}
	
	private Artist findOrCreate(EntityManager em, Artist draft) {
		Artist a0 = em.find(Artist.class, draft.getId());
		
		if (a0  == null) {
			a0 = draft;
			
			em.persist(a0);
		}
		
		return a0;
	}
}
