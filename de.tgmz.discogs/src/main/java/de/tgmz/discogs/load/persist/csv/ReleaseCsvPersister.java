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
	private int fid = 1;
	private int iid = 1;

	public ReleaseCsvPersister(String target) {
		this (target, c -> true);
	}
	
	public ReleaseCsvPersister(String target, Predicate<Release> filter) {
		super(target
				, Table.Release, Table.Track, Table.SubTrack
				, Table.Release_Track, Table.Track_SubTrack, Table.Release_Genre, Table.Release_Style, Table.Release_Artist
				, Table.Series.useCache(120_000)
				, Table.Track_Artist, Table.ReleaseExtraArtist, Table.ReleaseExtraArtist_applicableTracks
				, Table.Track_ExtraArtist, Table.SubTrack_ExtraArtist, Table.ExtraArtist
				, Table.Company.useCache(1_300_000), Table.EntityType.useCache(100)
				, Table.Release_labels, Table.ReleaseCompany
				, Table.Format, Table.Format_descriptions, Table.Release_Format, Table.FormatName, Table.format_gen
				, Table.Identifier, Table.Release_Identifier, Table.identifier_gen
				, Table.artist_release_all.useCache(5_000_000)
				, Table.label_release_all.useCache(1_500_000)
		);
		
		this.filter = filter;
	}
	
	protected int doSave(Release r) throws IOException {
		if (!filter.test(r)) return 0;
		
		Series ser = r.getSeries();
		
		if (ser != null) {
			pm.get(Table.Series).printRecordUsingCache(
				ser.getId()
				, ser.getCatno()
				, ser.getName()
			);
		}
		
		pm.get(Table.Release).printRecord(
			String.valueOf(r.isMain()).toUpperCase()
			, r.getDataQuality().ordinal()
			, r.getId()
			, r.getMaster() !=  null && r.getMaster().getId() > 0 ? r.getMaster().getId() : null
			, ser != null && ser.getId() > 0 ? ser.getId() : null
			, r.getAlbumArtist()
			, r.getCountry()
			, r.getReleased()
			, r.getTitle()
		);
		
		for (Genre g : r.getGenres()) {
			pm.get(Table.Release_Genre).printRecord(
				r.getId()
				, g.getId()
			);
		}
		
		for (Style s : r.getStyles()) {
			pm.get(Table.Release_Style).printRecord(
				r.getId()
				, s.getId()
			);
		}
		
		for (Label l : r.getLabels().keySet()) {
			pm.get(Table.label_release_all).printRecordUsingCache(
				l.getId()
				, l.getName()
			);
		}
		
		for (Entry<Label, String> e : r.getLabels().entrySet()) {
			pm.get(Table.Release_labels).printRecord(
				r.getId()
				, e.getKey().getId()
				, e.getValue()
			);
		}
		
		saveComplexAttributes(r);
		
		return 1;
	}

	private void saveComplexAttributes(Release r) throws IOException {
		for (Artist a : r.getArtists()) {
			save(r, a);
		}
		
		for (ReleaseCompany rc : r.getReleaseCompanies()) {
			save(r, rc);
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
	}

	private void save(Release r, ReleaseCompany rc) throws IOException {
		pm.get(Table.ReleaseCompany).printRecord(
			rc.getCompany().getId()
			, rc.getEntityType().getId()
			, r.getId()
		);

		pm.get(Table.Company).printRecordUsingCache(
			rc.getCompany().getId()
			, rc.getCompany().getName()
		);
			
		pm.get(Table.EntityType).printRecordUsingCache(
			rc.getEntityType().getId()
			, rc.getEntityType().getName()	
		);
	}
	private void save(Track t) throws IOException {
		pm.get(Table.Track).printRecord(
				t.getId().getRelease().getId()
				, t.getSequence()
				, t.getTrackNumber()
				, t.getTitle()
				, t.getDuration()
				, t.getPosition()
			);
			
		pm.get(Table.Release_Track).printRecord(
			t.getId().getRelease().getId()
			, t.getId().getRelease().getId()
			, t.getSequence()
		);

		for (Artist a : t.getArtists()) {
			pm.get(Table.Track_Artist).printRecord(
					t.getId().getRelease().getId()
					, t.getSequence()
					, a.getId()
				);

			pm.get(Table.artist_release_all).printRecordUsingCache(
					a.getId()
					, a.getName()
			);
		}
			
		for (ExtraArtist ea : t.getExtraArtists()) {
			pm.get(Table.Track_ExtraArtist).printRecord(
					t.getId().getRelease().getId()
					, t.getSequence()
					, ea.getArtist().getId()
					, ea.getRole()
				);
		
			pm.get(Table.artist_release_all).printRecordUsingCache(
					ea.getArtist().getId()
					, ea.getArtist().getName()
			);
		}
			
		for (SubTrack st : t.getSubTracklist()) {
			save(t, st);
		}
	}
	private void save(Track t, SubTrack st) throws IOException {
		pm.get(Table.SubTrack).printRecord(
				st.getSubTrackNumber()
				, t.getId().getRelease().getId()
				, t.getSequence()
				, st.getTitle()
				, st.getDuration()
				, st.getPosition()
			);

		pm.get(Table.Track_SubTrack).printRecord(
			t.getId().getRelease().getId()
			, t.getSequence()
			, st.getSubTrackNumber()
			, t.getId().getRelease().getId()
			, t.getSequence()
		);
				
		for (ExtraArtist ea : st.getExtraArtists()) {
			pm.get(Table.SubTrack_ExtraArtist).printRecord(
					st.getSubTrackNumber()
					, t.getId().getRelease().getId()
					, t.getSequence()
					, ea.getArtist().getId()
					, ea.getRole()
			);
					
			pm.get(Table.artist_release_all).printRecordUsingCache(
					ea.getArtist().getId()
					, ea.getArtist().getName()
			);
		}
	}
	private void save(ReleaseExtraArtist rea) throws IOException {
		if (rea.getExtraArtist().getRole() != null) {
			pm.get(Table.ReleaseExtraArtist).printRecord(
					rea.getExtraArtist().getArtist().getId()
					, rea.getRelease().getId()
					, rea.getExtraArtist().getRole()
					);
			
			for (String at : rea.getApplicableTracks()) {
				pm.get(Table.ReleaseExtraArtist_applicableTracks).printRecord(
					rea.getExtraArtist().getArtist().getId()
					, rea.getRelease().getId()
					, rea.getExtraArtist().getRole()
					, at
				);
			}
		} else {
			LOG.error("Role is NULL on {}", rea);
		}
		
		pm.get(Table.artist_release_all).printRecordUsingCache(
				rea.getExtraArtist().getArtist().getId()
				, rea.getExtraArtist().getArtist().getName()
		);

	}
	private void save(Release r, Artist a) throws IOException {
		pm.get(Table.Release_Artist).printRecord(
				r.getId()
				, a.getId()
			);
			
		pm.get(Table.artist_release_all).printRecordUsingCache(
				a.getId()
				, a.getName()
		);
	}
	
	private void save(Release r, Format f) throws IOException {
		pm.get(Table.Format).printRecord(
				fid
				, f.getQty()
				, f.getName().getId()
				, f.getText()
			);
				
		pm.get(Table.Release_Format).printRecord(
				r.getId()
				, fid
			);
					
		for (String s : f.getDescriptions()) {
			pm.get(Table.Format_descriptions).printRecord(
				fid
				, s
			);
		}
			
		++fid;
	}
	private void save(Release r, Identifier i) throws IOException {
		pm.get(Table.Identifier).printRecord(
				i.getType().ordinal()
				, iid
				, i.getDescription()
				, i.getValue()
			);
				
		pm.get(Table.Release_Identifier).printRecord(
				r.getId()
				, iid
			);
					
		++iid;
	}
}
 