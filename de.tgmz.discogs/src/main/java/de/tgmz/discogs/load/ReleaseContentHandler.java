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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;

public class ReleaseContentHandler extends DiscogsContentHandler {
	private List<String> displayArtists;
	private List<String> displayJoins;
	private int trackNumber;
	private int subTrackNumber;
	private long maxId;

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		maxId = (long) em.createNativeQuery("SELECT COALESCE(MAX(id), 0) FROM Release").getSingleResult();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("Skip releases up to id {}", String.format("%,d", maxId));
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[releases, release]":
			discogs = new Release();
			
			((Release) discogs).setId(Long.parseLong(attributes.getValue("id")));
			((Release) discogs).setStatus(attributes.getValue("status"));
			
			break;
		case "[releases, release, master_id]":
			((Release) discogs).setMain(Boolean.valueOf(attributes.getValue("is_main_release")));

			break;
		case "[releases, release, artists]":
			displayArtists = new ArrayList<>();
			displayJoins = new ArrayList<>();
			
			discogs.setArtists(new LinkedList<>());
			
			break;
		case "[releases, release, artists, artist]":
			discogs.getArtists().add(new Artist());
			
			break;
		case "[releases, release, extraartists]":
			((Release) discogs).setExtraArtists(new LinkedList<>());
			
			break;
		case "[releases, release, extraartists, artist]":
			((Release) discogs).getExtraArtists().add(new ExtraArtist());
			((Release) discogs).getExtraArtists().getLast().setArtist(new Artist());
		
			break;
		case "[releases, release, tracklist]":
			((Release) discogs).setTracklist(new LinkedList<>());
			trackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track]":
			Track t = new Track();
			t.setTrackNumber(trackNumber++);
			((Release) discogs).getTracklist().add(t);
			
			break;
		case "[releases, release, tracklist, track, artists]":
			((Release) discogs).getTracklist().getLast().setArtists(new LinkedList<>());
			
			break;
		case "[releases, release, tracklist, track, artists, artist]":
			((Release) discogs).getTracklist().getLast().getArtists().add(new Artist());
			
			break;
		case "[releases, release, tracklist, track, extraartists]":
			((Release) discogs).getTracklist().getLast().setExtraArtists(new LinkedList<>());
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist]":
			((Release) discogs).getTracklist().getLast().getExtraArtists().add(new ExtraArtist());
			((Release) discogs).getTracklist().getLast().getExtraArtists().getLast().setArtist(new Artist());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks]":
			((Release) discogs).getTracklist().getLast().setSubTracklist(new LinkedList<>());
			subTrackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track]":
			SubTrack st = new SubTrack();
			st.setTrackNumber(subTrackNumber++);
			((Release) discogs).getTracklist().getLast().getSubTracklist().add(st);
			
			break;
		case "[releases, release, labels]":
			((Release) discogs).setLabels(new HashMap<>());
			
			break;
		case "[releases, release, labels, label]":
			String id = attributes.getValue("id");
			
			if (id != null) {
				Label l = new Label(); 
				l.setId(Long.parseLong(attributes.getValue("id")));
			
				String catno = attributes.getValue("catno");
			
				((Release) discogs).getLabels().put(l, catno);
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
			
			((Release) discogs).setMaster(m);

			break;
		case "[releases, release, title]":
			discogs.setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, released]":
			((Release) discogs).setReleased(getChars());
			
			break;
		case "[releases, release, data_quality]":
			discogs.setDataQuality(getChars());
			
			break;
		case "[releases, release, country]":
			((Release) discogs).setCountry(getChars());
			
			break;
		// artists
		case "[releases, release, artists]":
			discogs.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			
			break;
		case "[releases, release, artists, artist, id]":
			discogs.getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, artists, artist, name]":
			String s = getChars(MAX_LENGTH_DEFAULT);
			
			displayArtists.add(s);
			discogs.getArtists().getLast().setName(s);
			
			break;
		case "[releases, release, artists, artist, anv]":
			displayArtists.set(displayArtists.size() - 1, getChars());
			
			break;
		case "[releases, release, artists, artist, join]":
			displayJoins.add(getChars());
			
			break;
		// extraartists
		case "[releases, release, extraartists, artist, id]":
			((Release) discogs).getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));
				
			break;
		case "[releases, release, extraartists, artist, role]":
			((Release) discogs).getExtraArtists().getLast().setRole(getChars(MAX_LENGTH_DEFAULT));
				
			break;
		//tracks
		case "[releases, release, tracklist, track]":
			Track t = ((Release) discogs).getTracklist().getLast();
			
			if (t.getPosition() == null && t.getSubTracklist() == null) {
				// Not al "real" track (e.g. headline like "Roots" and "The Roots of Sepultura") 
				((Release) discogs).getTracklist().removeLast();
				--trackNumber;
			}
			
			break;
		case "[releases, release, tracklist, track, position]":
			((Release) discogs).getTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, duration]":
			((Release) discogs).getTracklist().getLast().setDuration(getChars());
			
			break;
		case "[releases, release, tracklist, track, title]":
			((Release) discogs).getTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
				
			break;
		case "[releases, release, tracklist, track, artists, artist, id]":
			((Release) discogs).getTracklist().getLast().getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, id]":
			((Release) discogs).getTracklist().getLast().getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));

			break;
		case "[releases, release, tracklist, track, extraartists, artist, role]":
			((Release) discogs).getTracklist().getLast().getExtraArtists().getLast().setRole(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, position]":
			((Release) discogs).getTracklist().getLast().getSubTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, title]":
			((Release) discogs).getTracklist().getLast().getSubTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release]":
			Release r = (Release) discogs;
			
			String s0 = r.getId() > maxId ? "Save" : "Skip";
			
			if (r.getId() % threshold == 0) {
				LOG.info("{} {}", s0, discogs);
			}
			
			if (!ignore && r.getId() > maxId) {
				discogs.setTitle(StringUtils.left(discogs.getTitle(),  MAX_LENGTH_DEFAULT));

				fillAtributes();
				
				save(discogs);
			}
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isInfoEnabled()) {
			LOG.info("{} releases inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
	
	public Release getRelease() {
		return (Release) discogs;
	}
	
	private void fillAtributes() {
		discogs.setArtists(getArtists(discogs.getArtists()));
		
		for (Track t : ((Release) discogs).getTracklist()) {
			t.setArtists(getArtists(t.getArtists()));
			
			if (t.getExtraArtists() != null) {
				for (ExtraArtist ea : t.getExtraArtists()) {
					ea.setArtist(em.find(Artist.class, ea.getArtist().getId()));
				}
				
				// Failsafe: Some extra artists refer to non-existent artists
				t.getExtraArtists().removeIf(x -> x.getArtist() == null);
			}
		}

		Master m = ((Release) discogs).getMaster();
		
		if (m != null) {
			((Release) discogs).setMaster(em.find(Master.class, m.getId()));
		}
		
		List<ExtraArtist> eal = ((Release) discogs).getExtraArtists();
		
		if (eal != null) {
			for (ExtraArtist ea : eal) {
				ea.setArtist(em.find(Artist.class, ea.getArtist().getId()));
			}
			
			// Failsafe: Some extra artists refer to non-existent artists
			eal.removeIf(x -> x.getArtist() == null);
		}
		
		((Release) discogs).setLabels(getLabels(((Release) discogs).getLabels()));
	}
	
	private List<Artist> getArtists(List<Artist> artists) {
		if (artists == null || artists.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Artist> result = new LinkedList<>();
		
		for (Artist a : artists) {
			Artist a0 = em.find(Artist.class, a.getId());
		
			if (a0 == null) {
				LOG.debug("Artist {} not found", a);
			} else {
				result.add(a0);
			}
		}
		
		return result;
	}
	
	private Map<Label, String> getLabels(Map<Label, String> labels) {
		if (labels == null || labels.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Label, String> result = new HashMap<>();
		
		for (Entry<Label, String> e : labels.entrySet()) {
			Label l = em.find(Label.class, e.getKey().getId());
			
			if (l != null) {
				result.put(l, e.getValue());
			} else {
				LOG.debug("Label with id {} not found", e.getKey().getId());
			}
		}
		
		return result;
	}
}
