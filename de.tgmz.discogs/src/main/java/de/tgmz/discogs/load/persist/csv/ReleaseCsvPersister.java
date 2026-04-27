/*********************************************************************
* Copyright (c) 21.04.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.csv;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Company;
import de.tgmz.discogs.domain.EntityType;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Format;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.ReleaseCompany;
import de.tgmz.discogs.domain.ReleaseExtraArtist;
import de.tgmz.discogs.domain.Series;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;

public class ReleaseCsvPersister extends AbstractCsvPersister<Release> {
	private static final Logger LOG = LoggerFactory.getLogger(ReleaseCsvPersister.class);
	private Predicate<Release> filter;
	private Set<Series> serieses = new HashSet<>();
	private Set<Company> companies = new HashSet<>();
	private Set<EntityType> entityTypes = new HashSet<>();
	private long fid = 0;

	public ReleaseCsvPersister(String target) {
		this (target, c -> true);
	}
	
	public ReleaseCsvPersister(String target, Predicate<Release> filter) {
		super(target
				, "Release", "Track", "SubTrack"
				, "Release_Track", "Track_SubTrack", "Release_Genre", "Release_Style", "Release_Artist"
				, "Series", "Track_Artist", "release_extraartist", "ReleaseExtraArtist_applicableTracks"
				, "Track_ExtraArtist", "SubTrack_ExtraArtist"
				, "ExtraArtist", "Company", "EntityType"
				, "Release_labels", "release_company"
				, "Format", "Format_descriptions", "Release_Format"
				, "artist_release_all", "artist_release_track_all", "artist_release_extraartist_all", "artist_release_track_extraartist_all", "artist_release_subtrack_extraartist_all");
		
		this.filter = filter;
	}
	
	@Override
	public int flush() {
		try {
			for (Series s : serieses) {
				m.get("Series").printRecord(
					s.getId()
					, s.getCatno()
					, s.getName()
				);
			}
			
			for (EntityType et : entityTypes) {
				m.get("EntityType").printRecord(
					et.getId()
					, et.getName()
				);
			}
			
			for (Company c : companies) {
				m.get("Company").printRecord(
					c.getId()
					, c.getName()
				);
			}

			super.flush();
		} catch (IOException e) {
			LOG.error("", e);
		}
		
		return 0;
	}
	
	protected void doSave(Release r) throws IOException {
		if (!filter.test(r)) return;
		
		Series ser = r.getSeries();
		
		if (ser != null) {
			serieses.add(ser);
		}
		
		entityTypes.addAll(r.getReleaseCompanies().stream().map(rc -> rc.getEntityType()).toList());
		companies.addAll(r.getReleaseCompanies().stream().map(rc -> rc.getCompany()).toList());
		
		m.get("Release").printRecord(
			String.valueOf(r.isMain()).toUpperCase()
			, r.getDataQuality().ordinal()
			, r.getId()
			, r.getMaster() !=  null && r.getMaster().getId() > 0 ? r.getMaster().getId() : null
			, ser != null && ser.getId() > 0 ? ser.getId() : null
			, r.getAlbumArtist()
			, r.getTitle()
			, r.getCountry()
			, r.getReleased()
		);
		
		for (Genre g : r.getGenres()) {
			m.get("Release_Genre").printRecord(
				r.getId()
				, g.getId()
			);
		}
		
		for (Style s : r.getStyles()) {
			m.get("Release_Style").printRecord(
				r.getId()
				, s.getId()
			);
		}
		
		for (Artist a : r.getArtists()) {
			save(r, a);
		}
		
		for (Entry<Label, String> e : r.getLabels().entrySet()) {
			m.get("Release_labels").printRecord(
				r.getId()
				, e.getKey().getId()
				, e.getValue()
			);
		}
		
		
		for (ReleaseCompany rc : r.getReleaseCompanies()) {
			m.get("release_company").printRecord(
					rc.getEntityType().getId()
					, rc.getCompany().getId()
					, r.getId()
				);
		}
		
		for (ReleaseExtraArtist rea : r.getReleaseExtraArtists()) {
			save(rea);
		}
		
		for (Format f : r.getFormats()) {
			save(r, f);
		}
		
		for (Track t : r.getUnfilteredTracklist()) {
			save(t);
		}
	}
	private void save(Track t) throws IOException {
		m.get("Track").printRecord(
				t.getSequence()
				, t.getTrackNumber()
				, t.getId().getRelease().getId()
				, t.getTitle()
				, t.getDuration()
				, t.getPosition()
			);
			
		m.get("Release_Track").printRecord(
			t.getSequence()
			, t.getId().getRelease().getId()
			, t.getId().getRelease().getId()
		);
			
		for (Artist a : t.getArtists()) {
			m.get("Track_Artist").printRecord(
					t.getSequence()
					, t.getId().getRelease().getId()
					, a.getId()
				);
				
			m.get("artist_release_track_all").printRecord(
					a.getId()
					, a.getName()
			);
		}
			
		for (ExtraArtist ea : t.getExtraArtists()) {
			m.get("Track_ExtraArtist").printRecord(
					t.getSequence()
					, t.getId().getRelease().getId()
					, ea.getArtist().getId()
					, ea.getRole()
				);
				
			m.get("artist_release_track_extraartist_all").printRecord(
					ea.getArtist().getId()
					, ea.getArtist().getName()
			);
		}
			
		for (SubTrack st : t.getSubTracklist()) {
			save(t, st);
		}
	}
	private void save(Track t, SubTrack st) throws IOException {
		m.get("SubTrack").printRecord(
				st.getSubTrackNumber()
				, t.getSequence()
				, t.getId().getRelease().getId()
				, st.getTitle()
				, st.getDuration()
				, st.getPosition()
			);

		m.get("Track_SubTrack").printRecord(
			t.getSequence()
			, st.getSubTrackNumber()
			, t.getSequence()
			, t.getId().getRelease().getId()
			, t.getId().getRelease().getId()
		);
				
		for (ExtraArtist ea : st.getExtraArtists()) {
			m.get("SubTrack_ExtraArtist").printRecord(
					st.getSubTrackNumber()
					, t.getSequence()
					, t.getId().getRelease().getId()
					, ea.getArtist().getId()
					, ea.getRole()
			);
					
			m.get("artist_release_subtrack_extraartist_all").printRecord(
					ea.getArtist().getId()
					, ea.getArtist().getName()
			);
		}
	}
	private void save(ReleaseExtraArtist rea) throws IOException {
		if (rea.getExtraArtist().getRole() != null) {
			m.get("release_extraartist").printRecord(
					rea.getExtraArtist().getArtist().getId()
					, rea.getRelease().getId()
					, rea.getExtraArtist().getRole()
					);
			
			for (String at : rea.getApplicableTracks()) {
				m.get("ReleaseExtraArtist_applicableTracks").printRecord(
					rea.getExtraArtist().getArtist().getId()
					, rea.getRelease().getId()
					, rea.getExtraArtist().getRole()
					, at
				);
			}
		} else {
			LOG.error("Role is NULL on {}", rea);
		}
		
		m.get("artist_release_extraartist_all").printRecord(
				rea.getExtraArtist().getArtist().getId()
				, rea.getExtraArtist().getArtist().getName()
		);

	}
	private void save(Release r, Artist a) throws IOException {
		m.get("Release_Artist").printRecord(
				r.getId()
				, a.getId()
			);
			
		m.get("artist_release_all").printRecord(
				a.getId()
				, a.getName()
		);
	}
	private void save(Release r, Format f) throws IOException {
		m.get("Format").printRecord(
				fid
				, f.getName()
				, f.getQty()
				, f.getText()
			);
				
			m.get("Release_Format").printRecord(
					r.getId()
					, fid
				);
					
			for (String s : f.getDescriptions()) {
				m.get("Format_descriptions").printRecord(
						fid
						, s
				);
			}
			
			++fid;
	}
}
