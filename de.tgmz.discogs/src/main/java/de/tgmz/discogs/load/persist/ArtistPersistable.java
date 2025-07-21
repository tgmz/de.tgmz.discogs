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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.load.factory.ArtistFactory;
import jakarta.persistence.EntityManager;

public class ArtistPersistable implements IPersistable<Artist> {
	private Predicate<Artist> filter;
	private ArtistFactory af;
	
	public ArtistPersistable() {
		this(x -> true);
	}

	public ArtistPersistable(Predicate<Artist> filter) {
		this.filter = filter;
		
		af = new  ArtistFactory();
	}

	@Override
	public int save(EntityManager em, Artist artist) {
		if (filter.test(artist)) {
			Artist a0 = af.get(artist);
			
			a0.getMembers().replaceAll(a -> a = af.get(a));
			// In case an artist is both member and alias
			a0.getAliases().replaceAll(a -> getOrCreate(a, artist.getMembers()));
			
			em.merge(a0);
			
			return 1;
		} else {
			return 0;
		}
	}
	private Artist getOrCreate(Artist a0, List<Artist> as) {
		Optional<Artist> any = as.stream().filter(a -> a.getId() == a0.getId()).findAny();
		
		return any.isPresent() ? any.get() : af.get(a0);
	}
}
