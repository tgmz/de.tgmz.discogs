/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ReleaseExtraArtist;
import de.tgmz.discogs.domain.Role;
import jakarta.persistence.EntityManager;

public class ReleaseExtraArtistFactory implements IFactory<ReleaseExtraArtist> {
	private static IFactory<Artist> af = new ArtistFactory();
	private static IFactory<Role> rf = new RoleFactory();
	
	@Override
	public ReleaseExtraArtist get(EntityManager em, ReleaseExtraArtist draft) {
		draft.setArtist(af.get(em, draft.getArtist()));
		draft.setRole(rf.get(em, draft.getRole()));
		
		return draft;
	}
}
