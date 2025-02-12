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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Track;

public class ReleaseContentHandler extends DiscogsContentHandler {
	private List<String> displayArtists;
	private List<String> displayJoins;
	private int trackNumber;
	protected boolean ignore;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[release, releases]":
			discogs = new Release();
			
			((Release) discogs).setId(Long.parseLong(attributes.getValue("id")));
			((Release) discogs).setStatus(attributes.getValue("status"));
			
			break;
		case "[artists, release, releases]":
			displayArtists = new ArrayList<>();
			displayJoins = new ArrayList<>();
			
			discogs.setArtists(new LinkedList<>());
			
			break;
		case "[artist, artists, release, releases]":
			discogs.getArtists().add(new Artist());
			
			break;
		case "[extraartists, release, releases]":
			((Release) discogs).setExtraArtists(new LinkedList<>());
			
			break;
		case "[artist, extraartists, release, releases]":
			((Release) discogs).getExtraArtists().add(new ExtraArtist());
			((Release) discogs).getExtraArtists().getLast().setArtist(new Artist());
		
			break;
		case "[master_id, release, releases]":
			((Release) discogs).setMain(Boolean.valueOf(attributes.getValue("is_main_release")));

			break;
		case "[tracklist, release, releases]":
			((Release) discogs).setTracklist(new LinkedList<>());
			trackNumber = 1;
			
			break;
		case "[track, tracklist, release, releases]":
			Track t = new Track();
			t.setTrackNumber(trackNumber++);
			((Release) discogs).getTracklist().add(t);
			
			break;
		case "[artists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().setArtists(new LinkedList<>());
			
			break;
		case "[artist, artists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().getArtists().add(new Artist());
			
			break;
		case "[extraartists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().setExtraArtists(new LinkedList<>());
			
			break;
		case "[artist, extraartists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().getExtraArtists().add(new ExtraArtist());
			((Release) discogs).getTracklist().getLast().getExtraArtists().getLast().setArtist(new Artist());
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[master_id, release, releases]":
			Master m = new Master();
			m.setId(Long.parseLong(getChars()));
			
			((Release) discogs).setMaster(m);

			break;
		case "[id, artist, artists, release, releases]":
			discogs.getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[id, artist, artists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[name, artist, artists, release, releases]":
			displayArtists.add(getChars());
			discogs.getArtists().getLast().setName(getChars());
			
			break;
		case "[anv, artist, artists, release, releases]":
			displayArtists.set(displayArtists.size() - 1, getChars());
			
			break;
		case "[join, artist, artists, release, releases]":
			displayJoins.add(getChars());
			
			break;
		case "[position, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().setPosition(getChars());
			
			break;
		case "[duration, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().setDuration(getChars());
			
			break;
		case "[artists, release, releases]":
			discogs.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			
			break;
		case "[title, release, releases]":
			discogs.setTitle(StringUtils.left(getChars(), MAX_LENGTH_DEFAULT));
			
			break;
		case "[title, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().setTitle(StringUtils.left(getChars(), MAX_LENGTH_DEFAULT));
				
			break;
		case "[released, release, releases]":
			((Release) discogs).setReleased(getChars());
			
			break;
		case "[data_quality, release, releases]":
			discogs.setDataQuality(getChars());
			
			break;
		case "[country, release, releases]":
			((Release) discogs).setCountry(getChars());
			
			break;
		case "[id, artist, extraartists, release, releases]":
			((Release) discogs).getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));
			
			break;
		case "[id, artist, extraartists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));

			break;
		case "[role, artist, extraartists, release, releases]":
			((Release) discogs).getExtraArtists().getLast().setRole(StringUtils.left(getChars(), MAX_LENGTH_DEFAULT));
			
			break;
		case "[role, artist, extraartists, track, tracklist, release, releases]":
			((Release) discogs).getTracklist().getLast().getExtraArtists().getLast().setRole(StringUtils.left(getChars(), MAX_LENGTH_DEFAULT));
			
			break;
		case "[release, releases]":
			if (((Release) discogs).getId() % threshold == 0) {
				LOG.info("Save {}", discogs);
			}
			
			if (!ignore) {
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
}
