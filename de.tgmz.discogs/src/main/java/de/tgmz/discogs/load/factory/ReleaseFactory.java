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
import de.tgmz.discogs.domain.Series;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.factory.collections.MapFactory;
import de.tgmz.discogs.load.factory.collections.SetFactory;
import de.tgmz.discogs.relevance.RelevanceService;
import jakarta.persistence.EntityManager;

public class ReleaseFactory implements IFactory<Release> {
	private RelevanceService rs;
	private IFactory<Artist> af;
	private IFactory<ExtraArtist> eaf;
	private IFactory<Label> lf;
	private IFactory<ReleaseCompany> rcf;
	private IFactory<ReleaseExtraArtist> reaf;
	
	public ReleaseFactory() {
		rs = RelevanceService.getInstance();
		
		af = new ArtistFactory();
		eaf = new ExtraArtistFactory();
		
		// Do not use a real factory here. It will never return null and we want to remove non-existing labels
		lf = (EntityManager x, Label l) -> x.find(Label.class, l.getId());
		
		rcf = new ReleaseCompanyFactory();
		reaf = new ReleaseExtraArtistFactory();
	}
	
	@Override
	public Release get(EntityManager em, Release draft) {
		SetFactory<Artist> sfa = new SetFactory<>(em, af);
		SetFactory<ExtraArtist> sfea = new SetFactory<>(em, eaf);
		SetFactory<ReleaseCompany> sfrc = new SetFactory<>(em, rcf);
		
		MapFactory<Label, String> mfls = new MapFactory<>(em, lf);
		
		SetFactory<ReleaseExtraArtist> sfrea = new SetFactory<>(em, reaf);

		if (draft.getMaster() !=  null) {
			draft.setMaster(em.find(Master.class, draft.getMaster().getId()));
		}
		
		draft.setLabels(mfls.replaceAll(draft.getLabels()));

		draft.setArtists(sfa.replaceAll(draft.getArtists()));
		
		draft.setExtraArtists(sfrea.replaceAll(draft.getReleaseExtraArtists()));
		
		for (Track t : draft.getUnfilteredTracklist()) {
			t.setArtists(sfa.replaceAll(t.getArtists()));
			
			for (SubTrack st : t.getSubTracklist()) {
				st.setExtraArtists(sfea.replaceAll(st.getExtraArtists()));
			}
			
			t.setExtraArtists(sfea.replaceAll(t.getExtraArtists()));
		}
		
		draft.setReleaseCompanies(sfrc.replaceAll(draft.getReleaseCompanies()));
		
		if (!rs.isRelevant(Series.class)) {
			draft.setSeries(null);
		}
		
		return draft;
	}
}
