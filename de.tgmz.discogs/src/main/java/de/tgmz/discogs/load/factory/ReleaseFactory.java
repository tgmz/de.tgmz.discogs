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
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.factory.collections.MapFactory;
import de.tgmz.discogs.load.factory.collections.SetFactory;
import jakarta.persistence.EntityManager;

public class ReleaseFactory implements IFactory<Release> {
	private ArtistFactory af;
	private ExtraArtistFactory eaf;
	
	public ReleaseFactory() {
		af = new ArtistFactory();
		eaf = new ExtraArtistFactory();
	}
	
	@Override
	public Release get(EntityManager em, Release draft) {
		SetFactory<Artist> sfa = new SetFactory<>(em, af);
		SetFactory<Genre> sfg = new SetFactory<>(em, new GenreFactory());
		SetFactory<Style> sfs = new SetFactory<>(em, new StyleFactory());
		SetFactory<ExtraArtist> sfea = new SetFactory<>(em, eaf);
		
		// Do not use the LabelFactory here. It will never return null and we want to remove non-existing labels
		IFactory<Label> lf = (EntityManager x, Label l) -> x.find(Label.class, l.getId());
		MapFactory<Label, String> mfls = new MapFactory<>(em, lf);
		
		MapFactory<ExtraArtist, String> mfeas = new MapFactory<>(em, new ExtraArtistFactory());
		
		CompanyFactory cf = new CompanyFactory();

		if (draft.getMaster() !=  null) {
			draft.setMaster(em.find(Master.class, draft.getMaster().getId()));
		}
		
		draft.setLabels(mfls.replaceAll(draft.getLabels()));

		draft.setGenres(sfg.replaceAll(draft.getGenres()));
		draft.setStyles(sfs.replaceAll(draft.getStyles()));		
		draft.setArtists(sfa.replaceAll(draft.getArtists()));
		
		draft.setExtraArtists(mfeas.replaceAll(draft.getExtraArtists()));
		
		for (Track t : draft.getUnfilteredTracklist()) {
			t.setArtists(sfa.replaceAll(t.getArtists()));
			
			for (SubTrack st : t.getSubTracklist()) {
				st.setExtraArtists(sfea.replaceAll(st.getExtraArtists()));
			}
			
			t.setExtraArtists(sfea.replaceAll(t.getExtraArtists()));
		}
		
		draft.getCompanies().forEach(cr -> cr.getId().setCompany(cf.get(em, cr.getId().getCompany())));
		
		return draft;
	}
}
