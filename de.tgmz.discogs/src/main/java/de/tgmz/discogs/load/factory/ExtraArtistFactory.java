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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.id.ExtraArtistId;
import jakarta.persistence.EntityManager;

public class ExtraArtistFactory implements IFactory<ExtraArtist> {
	private static final Logger LOG = LoggerFactory.getLogger(ExtraArtistFactory.class);
	
	@Override
	public ExtraArtist get(EntityManager em, ExtraArtist draft) {
		return findOrCreate(em, draft);
	}
	
	private ExtraArtist findOrCreate(EntityManager em, ExtraArtist draft) {
		Artist a = em.find(Artist.class, draft.getArtist().getId());
		
		if (a == null) {
			a = draft.getArtist();

			LOG.trace("{} not present, creating...", a);
			
			em.persist(a);
		}
		
		draft.setArtist(a);
		
		ExtraArtist ea = em.find(ExtraArtist.class, new ExtraArtistId(a, draft.getRole()));
		
		if (ea == null) {
			LOG.trace("ExtraArtist {} not present, creating...", draft);
			
			ea = draft;
			
			em.persist(ea);
		}
		
		return ea;
	}
}
