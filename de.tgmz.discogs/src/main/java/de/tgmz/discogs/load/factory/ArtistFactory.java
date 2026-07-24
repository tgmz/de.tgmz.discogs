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

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.load.factory.collections.SetFactory;
import jakarta.persistence.EntityManager;

public class ArtistFactory implements IFactory<Artist> {
	private EntityManager em;
	private BasicEntityFactory<Artist> baf;
	private SetFactory<Artist> saf;
	
	public ArtistFactory() {
		baf = new BasicEntityFactory<>();
		
		saf = new SetFactory<>(baf);
	}
	
	@Override
	public Artist get(EntityManager em, Artist draft) {
		this.em = em;
		
		return enrich(draft);
	}
	
	private Artist enrich(Artist draft) {
		Artist a0 = baf.get(em, draft);
		
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
		
		if (a0.getRealname() == null) {
			a0.setRealname(draft.getRealname());
		}
		
		if (a0.getVariations().isEmpty()) {
			a0.setVariations(draft.getVariations());
		}
		
		a0.setAliases(saf.replaceAll(em, a0.getAliases()));
		a0.setGroups(saf.replaceAll(em, a0.getGroups()));
		a0.setMembers(saf.replaceAll(em, a0.getMembers()));
		
		return a0;
	}
}
