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

import de.tgmz.discogs.domain.ExtraArtist;
import jakarta.persistence.EntityManager;

public class ExtraArtistFactory implements IFactory<ExtraArtist> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ExtraArtistFactory.class);
	private Map<Pair<Long, String>, ExtraArtist> cache;
	private ArtistFactory af;
	
	public ExtraArtistFactory(ArtistFactory af) {
		this.af = af;
		cache = new HashMap<>();
	}

	@Override
	public ExtraArtist get(EntityManager em, ExtraArtist draft) {
		return cache.computeIfAbsent(Pair.of(draft.getArtist().getId(), draft.getRole()) , a -> findOrCreate(em, draft));
	}
	
	private ExtraArtist findOrCreate(EntityManager em, ExtraArtist draft) {
		ExtraArtist ea = em.createNamedQuery("ExtraArtist.byArtistIdAndRole", ExtraArtist.class)
				.setParameter(1, draft.getArtist().getId())
				.setParameter(2, draft.getRole())
				.getSingleResultOrNull();
		
		if (ea  == null) {
			ea = draft;
			
			ea.setArtist(af.get(em, ea.getArtist()));
		}
		
		return ea;
	}
}
