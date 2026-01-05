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
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.load.factory.collections.SetFactory;
import jakarta.persistence.EntityManager;

public class MasterFactory implements IFactory<Master> {
	
	@Override
	public Master get(EntityManager em, Master draft) {
		SetFactory<Artist> sra = new SetFactory<>(em, new ArtistFactory());
		
		draft.setArtists(sra.replaceAll(draft.getArtists()));
		
		return draft;
	}
}
