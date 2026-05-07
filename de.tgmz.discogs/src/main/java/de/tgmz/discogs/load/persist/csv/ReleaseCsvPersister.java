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
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Format;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Identifier;
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
	private long fid = 0;
	private long iid = 0;

	public ReleaseCsvPersister(String target) {
		this (target, c -> true);
	}
	
	public ReleaseCsvPersister(String target, Predicate<Release> filter) {
		super(target
				, Table.Release, Table.Track, Table.SubTrack
				, Table.Release_Track, Table.Track_SubTrack, Table.Release_Genre, Table.Release_Style, Table.Release_Artist
				, Table.Series, Table.Track_Artist, Table.release_extraartist, Table.ReleaseExtraArtist_applicableTracks
				, Table.Track_ExtraArtist, Table.SubTrack_ExtraArtist
				, Table.ExtraArtist, Table.Company, Table.EntityType
				, Table.Release_labels, Table.release_company
				, Table.Format, Table.Format_descriptions, Table.Release_Format
				, Table.artist_release_all, Table.artist_release_track_all, Table.artist_release_extraartist_all, Table.artist_release_track_extraartist_all, Table.artist_release_subtrack_extraartist_all
				, Table.Identifier, Table.Release_Identifier
		);
		
		this.filter = filter;
	}
	
	protected int doSave(Release r) throws IOException {
		if (!filter.test(r)) return 0;
		
		Series ser = r.getSeries();
		
		if (ser != null) {
			m.get(Table.Series).printRecordUsingCache(
				ser.getId()
				, ser.getCatno()
				, ser.getName()
			);
		}
		
		m.get(Table.Release).printRecord(
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
			m.get(Table.Release_Genre).printRecord(
				r.getId()
				, g.getId()
			);
		}
		
		for (Style s : r.getStyles()) {
			m.get(Table.Release_Style).printRecord(
				r.getId()
				, s.getId()
			);
		}
		
		for (Artist a : r.getArtists()) {
			save(r, a);
		}
		
		for (Entry<Label, String> e : r.getLabels().entrySet()) {
			m.get(Table.Release_labels).printRecord(
				r.getId()
				, e.getKey().getId()
				, e.getValue()
			);
		}
		
		
		for (ReleaseCompany rc : r.getReleaseCompanies()) {
			m.get(Table.release_company).printRecord(
				rc.getEntityType().getId()
				, rc.getCompany().getId()
				, r.getId()
			);
			
			m.get(Table.Company).printRecordUsingCache(
				rc.getCompany().getId()
				, rc.getCompany().getName()
			);
				
			m.get(Table.EntityType).printRecordUsingCache(
				rc.getEntityType().getId()
				, rc.getEntityType().getName()	
			);
		}
		
		for (ReleaseExtraArtist rea : r.getReleaseExtraArtists()) {
			save(rea);
		}
		
		for (Format f : r.getFormats()) {
			save(r, f);
		}
		
		for (Identifier i : r.getIdentifiers()) {
			save(r, i);
		}
		
		for (Track t : r.getUnfilteredTracklist()) {
			save(t);
		}
		
		return 1;
	}
	private void save(Track t) throws IOException {
		m.get(Table.Track).printRecord(
				t.getSequence()
				, t.getTrackNumber()
				, t.getId().getRelease().getId()
				, t.getTitle()
				, t.getDuration()
				, t.getPosition()
			);
			
		m.get(Table.Release_Track).printRecord(
			t.getSequence()
			, t.getId().getRelease().getId()
			, t.getId().getRelease().getId()
		);
			
		for (Artist a : t.getArtists()) {
			m.get(Table.Track_Artist).printRecord(
					t.getSequence()
					, t.getId().getRelease().getId()
					, a.getId()
				);
				
			m.get(Table.artist_release_track_all).printRecordUsingCache(
					a.getId()
					, a.getName()
			);
		}
			
		for (ExtraArtist ea : t.getExtraArtists()) {
			m.get(Table.Track_ExtraArtist).printRecord(
					t.getSequence()
					, t.getId().getRelease().getId()
					, ea.getArtist().getId()
					, ea.getRole()
				);
				
			m.get(Table.artist_release_track_extraartist_all).printRecordUsingCache(
					ea.getArtist().getId()
					, ea.getArtist().getName()
			);
		}
			
		for (SubTrack st : t.getSubTracklist()) {
			save(t, st);
		}
	}
	private void save(Track t, SubTrack st) throws IOException {
		m.get(Table.SubTrack).printRecord(
				st.getSubTrackNumber()
				, t.getSequence()
				, t.getId().getRelease().getId()
				, st.getTitle()
				, st.getDuration()
				, st.getPosition()
			);

		m.get(Table.Track_SubTrack).printRecord(
			t.getSequence()
			, st.getSubTrackNumber()
			, t.getSequence()
			, t.getId().getRelease().getId()
			, t.getId().getRelease().getId()
		);
				
		for (ExtraArtist ea : st.getExtraArtists()) {
			m.get(Table.SubTrack_ExtraArtist).printRecord(
					st.getSubTrackNumber()
					, t.getSequence()
					, t.getId().getRelease().getId()
					, ea.getArtist().getId()
					, ea.getRole()
			);
					
			m.get(Table.artist_release_subtrack_extraartist_all).printRecordUsingCache(
					ea.getArtist().getId()
					, ea.getArtist().getName()
			);
		}
	}
	private void save(ReleaseExtraArtist rea) throws IOException {
		if (rea.getExtraArtist().getRole() != null) {
			m.get(Table.release_extraartist).printRecord(
					rea.getExtraArtist().getArtist().getId()
					, rea.getRelease().getId()
					, rea.getExtraArtist().getRole()
					);
			
			for (String at : rea.getApplicableTracks()) {
				m.get(Table.ReleaseExtraArtist_applicableTracks).printRecord(
					rea.getExtraArtist().getArtist().getId()
					, rea.getRelease().getId()
					, rea.getExtraArtist().getRole()
					, at
				);
			}
		} else {
			LOG.error("Role is NULL on {}", rea);
		}
		
		m.get(Table.artist_release_extraartist_all).printRecordUsingCache(
				rea.getExtraArtist().getArtist().getId()
				, rea.getExtraArtist().getArtist().getName()
		);

	}
	private void save(Release r, Artist a) throws IOException {
		m.get(Table.Release_Artist).printRecord(
				r.getId()
				, a.getId()
			);
			
		m.get(Table.artist_release_all).printRecordUsingCache(
				a.getId()
				, a.getName()
		);
	}
	private void save(Release r, Format f) throws IOException {
		m.get(Table.Format).printRecord(
				fid
				, f.getName()
				, f.getQty()
				, f.getText()
			);
				
			m.get(Table.Release_Format).printRecord(
					r.getId()
					, fid
				);
					
			for (String s : f.getDescriptions()) {
				m.get(Table.Format_descriptions).printRecord(
						fid
						, s
				);
			}
			
			fid += 50;
	}
	
	private void save(Release r, Identifier i) throws IOException {
		m.get(Table.Identifier).printRecord(
				i.getType().ordinal()
				, iid
				, i.getDescription()
				, i.getValue()
			);
				
			m.get(Table.Release_Identifier).printRecord(
					r.getId()
					, iid
				);
			
			iid += 50;
	}
}
 