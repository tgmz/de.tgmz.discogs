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

import java.util.function.Predicate;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.load.factory.ArtistFactory;
import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;

public class ArtistPersistable implements IPersistable<Artist> {
	private Predicate<Artist> filter;
	
	public ArtistPersistable() {
		this(x -> true);
	}

	public ArtistPersistable(Predicate<Artist> filter) {
		this.filter = filter;
	}

	@Override
	public IFactory<Artist> getFactory() {
		return new ArtistFactory();
	}

	@Override
	public Predicate<Artist> getFilter() {
		return filter;
	}
	
	@Override
	public int save(EntityManager em, Artist a) {
		if (a.getId() == 0L) {
			return 0;
		}
		
		return IPersistable.super.save(em, a);
	}
}
