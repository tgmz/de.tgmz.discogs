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
import de.tgmz.discogs.domain.ExtraArtist;
import jakarta.persistence.EntityManager;

public class ExtraArtistFactory implements IFactory<ExtraArtist> {
	private BasicEntityFactory<Artist> baf;
	private BasicEntityFactory<ExtraArtist> beaf;

	public ExtraArtistFactory() {
		baf = new BasicEntityFactory<>();
		beaf = new BasicEntityFactory<>();
	}
	
	@Override
	public ExtraArtist get(EntityManager em, ExtraArtist draft) {
		Artist a = baf.get(em, draft.getArtist());
		
		draft.setArtist(a);
		
		return beaf.get(em, draft);
	}
}
