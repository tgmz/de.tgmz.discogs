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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.ExtraArtistId;
import jakarta.persistence.EntityManager;

public class ExtraArtistFactory implements IFactory<ExtraArtist> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ExtraArtistFactory.class);
	private Map<Pair<Long, String>, ExtraArtist> cache;
	
	public ExtraArtistFactory() {
		cache = new HashMap<>();
	}

	@Override
	public ExtraArtist get(EntityManager em, ExtraArtist draft) {
		return cache.computeIfAbsent(Pair.of(draft.getArtist().getId(), draft.getRole()) , a -> findOrCreate(em, draft));
	}
	
	private ExtraArtist findOrCreate(EntityManager em, ExtraArtist draft) {
		Artist a = em.find(Artist.class, draft.getArtist().getId());
		
		if (a == null) {
			a = draft.getArtist();

			em.persist(a);
		}
		
		String role = draft.getRole();
		
		ExtraArtist ea = em.find(ExtraArtist.class, new ExtraArtistId(a, role));
		
		if (ea  == null) {
			ea = draft;
			
			ea.setArtist(a);
		}
		
		return ea;
	}
}
