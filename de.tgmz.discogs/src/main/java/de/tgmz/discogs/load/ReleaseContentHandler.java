/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Company;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.EntityType;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Format;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.ReleaseCompany;
import de.tgmz.discogs.domain.ReleaseExtraArtist;
import de.tgmz.discogs.domain.Series;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.persist.ReleasePersistable;

public class ReleaseContentHandler extends DiscogsContentHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(ReleaseContentHandler.class);
	private List<String> bandArtists;
	private List<String> bandJoins;
	private short sequence;
	private short trackNumber;
	private short subTrackNumber;
	private Artist artist;
	private ReleaseExtraArtist releaseExtraArtist;
	private ExtraArtist extraArtist;
	private Track track;
	private SubTrack subTrack;
	private ReleaseCompany releaseCompany;
	private Company company;
	private EntityType entityType;
	private Release r;
	private Format format;

	public ReleaseContentHandler() {
		this (x -> true);
	}
	
	public ReleaseContentHandler(Predicate<Release> filter) {
		super();
		
		persister = new ReleasePersistable(filter);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[releases, release]":
			r = new Release();
			
			r.setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case "[releases, release, master_id]":
			r.setMain(Boolean.valueOf(attributes.getValue("is_main_release")));

			break;
		case "[releases, release, artists]":
			bandArtists = new ArrayList<>();
			bandJoins = new ArrayList<>();
			
			break;
		case "[releases, release, artists, artist]"
			, "[releases, release, tracklist, track, artists, artist]":
			artist = new Artist();
			
			break;
		case "[releases, release, extraartists, artist]":
			releaseExtraArtist = new ReleaseExtraArtist();
			releaseExtraArtist.setRelease(r);
			releaseExtraArtist.setArtist(new Artist());

		break;
		case "[releases, release, tracklist, track, extraartists, artist]"
			, "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist]":
			extraArtist = new ExtraArtist();

			break;
		case "[releases, release, tracklist]":
			sequence = 0;
			trackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track]":
			track = new Track(r);
			track.setSequence(sequence++);
			track.setTrackNumber(trackNumber++);
			
			break;
		case "[releases, release, tracklist, track, sub_tracks]":
			subTrackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track]":
			subTrack = new SubTrack(track);
			subTrack.setSubTrackNumber(subTrackNumber++);
			
			break;
		case "[releases, release, labels, label]":
			String id = attributes.getValue("id");
			
			if (id != null) {
				Label l = new Label(); 
				l.setId(Long.parseLong(attributes.getValue("id")));
			
				String catno = attributes.getValue("catno");
			
				r.getLabels().put(l, catno);
			}
			
			break;
		case "[releases, release, companies, company]":
			releaseCompany = new ReleaseCompany();
			company = new Company();
			entityType = new EntityType();
			
			break;
		case "[releases, release, formats, format]":
			format = new Format(attributes.getValue("name"), attributes.getValue("qty"), attributes.getValue("text"));
			
			break;
		case "[releases, release, series, series]":
			String s = attributes.getValue("id");
			
			if (StringUtils.isNotEmpty(s)) {
				r.setSeries(new Series(Long.parseLong(s), attributes.getValue("catno"), attributes.getValue("name")));
			}
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[releases, release, master_id]":
			Master m = new Master();
			m.setId(Long.parseLong(getChars()));
			
			r.setMaster(m);

			break;
		case "[releases, release, title]":
			r.setTitle(getChars());
			
			break;
		case "[releases, release, released]":
			r.setReleased(getChars());
			
			break;
		case "[releases, release, data_quality]":
			r.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[releases, release, country]":
			r.setCountry(getChars());
			
			break;
		// artists
		case "[releases, release, artists]":
			r.setAlbumArtist(computeBand(bandArtists, bandJoins));
			
			break;
		case "[releases, release, artists, artist]":
			// Don't add artist with empty id
			if (artist.getId() == 0L) {
				LOG.debug("Empty id on {}. Removing it", artist);
			} else {
				r.getArtists().add(artist);
			}
			
			break;
		case "[releases, release, artists, artist, id]"
			, "[releases, release, tracklist, track, artists, artist, id]":
			artist.setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, artists, artist, name]":
			String s = getChars(true);
			
			bandArtists.add(s);
			artist.setName(s);
			
			break;
		case "[releases, release, artists, artist, anv]":
			bandArtists.set(bandArtists.size() - 1, getChars());
			
			break;
		case "[releases, release, artists, artist, join]":
			bandJoins.add(getChars());
			
			break;
		case "[releases, release, genres, genre]":
			r.getGenres().add(new Genre(getChars()));
			
			break;
		case "[releases, release, styles, style]":
			r.getStyles().add(new Style(getChars()));
			
			break;
		// extraartists
			
		case "[releases, release, extraartists, artist, id]":
			releaseExtraArtist.getArtist().setId(Long.parseLong(getChars()));
				
			break;
		case "[releases, release, extraartists, artist, name]":
			releaseExtraArtist.getArtist().setName(getChars(true));
			
			break;
		case "[releases, release, extraartists, artist, role]":
			releaseExtraArtist.setRole(getChars());
			
			break;
		case "[releases, release, extraartists, artist, tracks]":
			releaseExtraArtist.setApplicableTracks(getChars());
				
			break;
		case "[releases, release, tracklist, track, extraartists, artist, id]"
			, "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, id]":
			extraArtist.getArtist().setId(Long.parseLong(getChars()));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, name]"
			, "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, name]":
			extraArtist.getArtist().setName(getChars(true));
		
			break;
		case "[releases, release, tracklist, track, extraartists, artist, role]"
			, "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, role]":
			extraArtist.setRole(getChars());
		
			break;
			
		case "[releases, release, extraartists, artist]":
			// Don't add extraArtist with empty artist.id
			if (releaseExtraArtist.getArtist().getId() == 0L) {
				LOG.debug("Empty id on {}. Removing it", releaseExtraArtist);
			} else {
				// Synchronize key and contents
				releaseExtraArtist.setRelease(r);
				releaseExtraArtist.setArtist(releaseExtraArtist.getArtist());
				releaseExtraArtist.setRole(releaseExtraArtist.getRole());
				
				r.getReleaseExtraArtists().add(releaseExtraArtist);
			}
				
			break;
		//tracks
		case "[releases, release, tracklist, track]":
			r.getUnfilteredTracklist().add(track);
			
			if (track.getPosition() == null && track.getSubTracklist().isEmpty()) {
				// Not al "real" track (e.g. headline like "Roots" and "The Roots of Sepultura") 
				--trackNumber;
			}
			
			break;
		case "[releases, release, tracklist, track, position]":
			track.setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, duration]":
			track.setDuration(getChars());
			
			break;
		case "[releases, release, tracklist, track, title]":
			track.setTitle(getChars());
				
			break;
		case "[releases, release, tracklist, track, artists, artist]":
			// Don't add artist with empty id
			if (artist.getId() != 0L) {
				track.getArtists().add(artist);
			}

			break;
		case "[releases, release, tracklist, track, artists, artist, name]":
			artist.setName(getChars(true));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist]":
			if (extraArtist.getArtist().getId() != 0L) {
				track.getExtraArtists().add(extraArtist);
			}
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track]":
			track.getSubTracklist().add(subTrack);
			
			break;

		case "[releases, release, tracklist, track, sub_tracks, track, position]":
			subTrack.setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, title]":
			subTrack.setTitle(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, duration]":
			subTrack.setDuration(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist]":
			if (extraArtist.getArtist().getId() != 0L) {
				subTrack.getExtraArtists().add(extraArtist);
			}
			
			break;
		case "[releases, release, companies, company, id]":
			company.setId(Long.parseLong(getChars()));
			
			break;
		case "[releases, release, companies, company, name]":
			company.setName(getChars());
			
			break;
		case "[releases, release, companies, company, entity_type]":
			entityType.setId(Byte.parseByte(getChars()));
			
			break;
		case "[releases, release, companies, company, entity_type_name]":
			entityType.setName(getChars());
			
			break;
		case "[releases, release, companies, company]":
			// cf release 3759: Copyright company "Infinite Jazz Recordings" carries no id. 
			if (company.getId() == null) {
				LOG.debug("Id is null for {} {}. Removing it", entityType, company);
			} else {
				releaseCompany.setRelease(r);
				releaseCompany.setCompany(company);
				releaseCompany.setEntityType(entityType);
			
				r.getReleaseCompanies().add(releaseCompany);
			}
			
			break;
		case "[releases, release, formats, format, descriptions, description]":
			format.getDescriptions().add(getChars());
			
			break;
		case "[releases, release, formats, format]":
			r.getFormats().add(format);
			
			break;
		case "[releases, release]":
			save(r);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
}
