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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.factory.GenreFactory;
import de.tgmz.discogs.load.factory.StyleFactory;
import de.tgmz.discogs.load.persist.ReleasePersistable;

public class ReleaseContentHandler extends DiscogsContentHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(ReleaseContentHandler.class);
	private List<String> displayArtists;
	private List<String> displayJoins;
	private int sequence;
	private int trackNumber;
	private int subTrackNumber;
	private ExtraArtist rea;
	private String tracks;
	private Release r;
	private Predicate<Discogs> filter;
	private GenreFactory genreFactory;
	private StyleFactory styleFactory;
	
	public ReleaseContentHandler() {
		this (x -> true);
	}
	
	public ReleaseContentHandler(Predicate<Discogs> filter) {
		super();
		
		this.filter = filter;
		
		genreFactory = new GenreFactory();
		styleFactory = new StyleFactory();
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
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
			displayArtists = new ArrayList<>();
			displayJoins = new ArrayList<>();
			
			break;
		case "[releases, release, artists, artist]":
			r.getArtists().add(new Artist());
			
			break;
		case "[releases, release, extraartists, artist]":
			rea = new ExtraArtist();
			tracks = "";
		
			break;
		case "[releases, release, tracklist]":
			sequence = 0;
			trackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track]":
			Track t = new Track();
			t.setSequence(sequence++);
			t.setTrackNumber(trackNumber++);
			r.getUnfilteredTracklist().add(t);
			
			break;
		case "[releases, release, tracklist, track, artists]":
			r.getUnfilteredTracklist().getLast().setArtists(new LinkedList<>());
			
			break;
		case "[releases, release, tracklist, track, artists, artist]":
			r.getUnfilteredTracklist().getLast().getArtists().add(new Artist());
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist]":
			r.getUnfilteredTracklist().getLast().getExtraArtists().add(new ExtraArtist());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks]":
			subTrackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track]":
			SubTrack st = new SubTrack();
			st.setTrackNumber(subTrackNumber++);
			r.getUnfilteredTracklist().getLast().getSubTracklist().add(st);
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist]":
			r.getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().add(new ExtraArtist());
			
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
			r.setTitle(getChars(MAX_LENGTH_DEFAULT));
			
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
			r.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			
			break;
		case "[releases, release, artists, artist, id]":
			r.getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, artists, artist, name]":
			String s = getChars(MAX_LENGTH_DEFAULT, true);
			
			displayArtists.add(s);
			r.getArtists().getLast().setName(s);
			
			break;
		case "[releases, release, artists, artist, anv]":
			displayArtists.set(displayArtists.size() - 1, getChars());
			
			break;
		case "[releases, release, artists, artist, join]":
			displayJoins.add(getChars());
			
			break;
		case "[releases, release, genres, genre]":
			r.getGenres().add(genreFactory.get(getChars()));
			
			break;
		case "[releases, release, styles, style]":
			r.getStyles().add(styleFactory.get(getChars()));
			
			break;
		// extraartists
		case "[releases, release, extraartists, artist, id]":
			rea.getArtist().setId(Long.parseLong(getChars()));
				
			break;
		case "[releases, release, extraartists, artist, name]":
			rea.getArtist().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, extraartists, artist, role]":
			rea.setRole(getChars());
			
			break;
		case "[releases, release, extraartists, artist, tracks]":
			tracks = getChars();
				
			break;
		case "[releases, release, extraartists, artist]":
			r.getExtraArtists().put(rea, tracks);
				
			break;
		//tracks
		case "[releases, release, tracklist, track]":
			Track t = r.getUnfilteredTracklist().getLast();
			
			if (t.getPosition() == null && t.getSubTracklist().isEmpty()) {
				// Not al "real" track (e.g. headline like "Roots" and "The Roots of Sepultura") 
				--trackNumber;
			}
			
			break;
		case "[releases, release, tracklist, track, position]":
			r.getUnfilteredTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, duration]":
			r.getUnfilteredTracklist().getLast().setDuration(getChars());
			
			break;
		case "[releases, release, tracklist, track, title]":
			r.getUnfilteredTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
				
			break;
		case "[releases, release, tracklist, track, artists, artist, id]":
			r.getUnfilteredTracklist().getLast().getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, tracklist, track, artists, artist, name]":
			r.getUnfilteredTracklist().getLast().getArtists().getLast().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, id]":
			r.getUnfilteredTracklist().getLast().getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));

			break;
		case "[releases, release, tracklist, track, extraartists, artist, name]":
			r.getUnfilteredTracklist().getLast().getExtraArtists().getLast().getArtist().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, role]":
			r.getUnfilteredTracklist().getLast().getExtraArtists().getLast().setRole(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, position]":
			r.getUnfilteredTracklist().getLast().getSubTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, title]":
			r.getUnfilteredTracklist().getLast().getSubTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, id]":
			r.getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, name]":
			r.getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().getLast().getArtist().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, role]":
			r.getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().getLast().setRole(getChars());
			
			break;
		case "[releases, release]":
			save(r);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
}
