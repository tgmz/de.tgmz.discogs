/*********************************************************************
* Copyright (c) 19.08.2025 Thomas Zierer
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
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.ReleaseCompany;
import de.tgmz.discogs.domain.ReleaseExtraArtist;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.factory.collections.MapFactory;
import de.tgmz.discogs.load.factory.collections.SetFactory;
import jakarta.persistence.EntityManager;

public class ReleaseFactory implements IFactory<Release> {
	private SetFactory<Artist> asf;					// ArtistSetFactory
	private SetFactory<ExtraArtist> easf;			// ExtraArtistSetFactory
	private SetFactory<ReleaseCompany> rcsf;		// ReleaseCompanySetFactory
	private MapFactory<Label, String> lmf;			// LabelMapFactory
	private SetFactory<ReleaseExtraArtist> reasf;	// ReleaseExtraArtistSetFactory
	
	public ReleaseFactory() {
		asf = new SetFactory<>(new ArtistFactory());
		easf = new SetFactory<>(new ExtraArtistFactory());
		rcsf = new SetFactory<>(new ReleaseCompanyFactory());
		
		lmf = new MapFactory<>(new AtomicEntityFactory<>());
		
		reasf = new SetFactory<>(new ReleaseExtraArtistFactory());
	}
	
	@Override
	public Release get(EntityManager em, Release draft) {
		if (draft.getMaster() !=  null) {
			draft.setMaster(em.find(Master.class, draft.getMaster().getId()));
		}
		
		draft.setLabels(lmf.replaceAll(em, draft.getLabels()));

		draft.setArtists(asf.replaceAll(em, draft.getArtists()));
		
		draft.setReleaseExtraArtists(reasf.replaceAll(em, draft.getReleaseExtraArtists()));
		
		for (Track t : draft.getUnfilteredTracklist()) {
			t.setArtists(asf.replaceAll(em, t.getArtists()));
			
			for (SubTrack st : t.getSubTracklist()) {
				st.setExtraArtists(easf.replaceAll(em, st.getExtraArtists()));
			}
			
			t.setExtraArtists(easf.replaceAll(em, t.getExtraArtists()));
		}
		
		draft.setReleaseCompanies(rcsf.replaceAll(em, draft.getReleaseCompanies()));
		
		return draft;
	}
}
