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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
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
		SetReplacer<Artist> sra = new SetReplacer<>(em, af);
		SetReplacer<Genre> srg = new SetReplacer<>(em, new GenreFactory());
		SetReplacer<Style> srs = new SetReplacer<>(em, new StyleFactory());
		SetReplacer<ExtraArtist> srea = new SetReplacer<>(em, eaf);
		
		CompanyFactory cf = new CompanyFactory();

		if (draft.getMaster() !=  null) {
			draft.setMaster(em.find(Master.class, draft.getMaster().getId()));
		}
		
		Map<Label, String> result = HashMap.newHashMap(draft.getLabels().size());
		
		for (Entry<Label, String> e : draft.getLabels().entrySet()) {
			Label l = em.find(Label.class, e.getKey().getId());
			
			if (l != null) {
				result.put(l, e.getValue());
			}
		}
			
		draft.setLabels(result);

		draft.setGenres(srg.replaceAll(draft.getGenres()));
		draft.setStyles(srs.replaceAll(draft.getStyles()));		
		draft.setArtists(sra.replaceAll(draft.getArtists()));
		draft.setExtraArtists(srea.replaceAll(draft.getExtraArtists()));
		
		for (Track t : draft.getUnfilteredTracklist()) {
			t.setArtists(sra.replaceAll(t.getArtists()));
			
			for (SubTrack st : t.getSubTracklist()) {
				st.setExtraArtists(srea.replaceAll(st.getExtraArtists()));
			}
			
			t.setExtraArtists(srea.replaceAll(t.getExtraArtists()));
		}
		
		draft.getCompanies().forEach(cr -> cr.getId().setCompany(cf.get(em, cr.getId().getCompany())));
		
		return draft;
	}
}
