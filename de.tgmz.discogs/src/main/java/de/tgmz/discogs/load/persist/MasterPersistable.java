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

import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.load.factory.ArtistFactory;
import jakarta.persistence.EntityManager;

public class MasterPersistable implements IPersistable<Master> {
	private Predicate<Master> filter;
	private ArtistFactory af;
	
	public MasterPersistable() {
		this(x -> true);
	}

	public MasterPersistable(Predicate<Master> filter) {
		this.filter = filter;
		
		af = new ArtistFactory();
	}

	@Override
	public int save(EntityManager em, Master master) {
		if (filter.test(master)) {
			master.setArtists(af.get(em, master.getArtists()));
			
			em.merge(master);
			
			return 1;
		} else {
			return 0;
		}
	}
}
