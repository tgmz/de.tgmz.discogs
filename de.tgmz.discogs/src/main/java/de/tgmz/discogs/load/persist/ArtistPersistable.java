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
import jakarta.persistence.EntityManager;

public class ArtistPersistable implements IPersistable<Artist> {
	private Predicate<Artist> filter;
	private ArtistFactory af;
	
	public ArtistPersistable() {
		this(x -> true);
	}

	public ArtistPersistable(Predicate<Artist> filter) {
		this.filter = filter;
		
		af = new ArtistFactory();
	}

	@Override
	public int save(EntityManager em, Artist draft) {
		if (filter.test(draft)) {
			
			Artist a0 = af.get(em, draft);
			
			em.merge(a0);
			
			return 1;
		} else {
			return 0;
		}
	}
}
