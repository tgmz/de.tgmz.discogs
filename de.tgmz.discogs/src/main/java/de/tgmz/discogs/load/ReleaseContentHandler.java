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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Track;

public class ReleaseContentHandler extends DiscogsContentHandler {
	private Artist artist;
	private ExtraArtist extraArtist;
	private Track track;
	private List<String> displayArtists;
	private List<String> displayJoins;

	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[release, releases]":
			discogs = new Release();
			
			((Release) discogs).setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case "[artists, release, releases]":
			displayArtists = new ArrayList<>();
			displayJoins = new ArrayList<>();
			
			break;
		case "[master_id, release, releases]":
			((Release) discogs).setMain(Boolean.valueOf(attributes.getValue("is_main_release")));

			break;
		case "[artist, artists, release, releases]":
		case "[artist, artists, track, tracklist, release, releases]":
			artist = new Artist();
			
			break;
		case "[tracklist, release, releases]":
			((Release) discogs).setTracklist(new LinkedList<>());
			
			break;
		case "[track, tracklist, release, releases]":
			track = new Track();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[id, artist, artists, release, releases]":
		case "[id, artist, artists, track, tracklist, release, releases]":
			artist.setId(Long.valueOf(getChars()));
			
			break;
		case "[master_id, release, releases]":
			((Release) discogs).setMasterId(Long.parseLong(getChars()));

			break;
		case "[name, artist, artists, release, releases]":
			displayArtists.add(getChars());
			
			break;
		case "[anv, artist, artists, release, releases]":
			displayArtists.set(displayArtists.size() - 1, getChars());
			
			break;
		case "[artist, artists, release, releases]":
			discogs.getArtistIds().add(artist.getId());
			break;
		case "[artist, artists, track, tracklist, release, releases]":
			track.getArtistIds().add(artist.getId());
			break;
		case "[join, artist, artists, release, releases]":
			displayJoins.add(getChars());
			
			break;
		case "[position, track, tracklist, release, releases]":
			track.setPosition(getChars());
			
			break;
		case "[artists, release, releases]":
			discogs.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			
			break;
		case "[duration, track, tracklist, release, releases]":
			track.setDuration(getChars());
			
			break;
		case "[title, release, releases]":
			if (discogs.getTitle() == null) {
				discogs.setTitle(getChars());
			}
			break;
				
		case "[title, track, tracklist, release, releases]":
			track.setTitle(StringUtils.left(getChars(), MAX_LENGTH_DEFAULT));
				
			((Release) discogs).getTracklist().add(track);
				
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
		case "[release, releases]":
			if (((Release) discogs).getId() % 10_000 == 0 && LOG.isInfoEnabled()) {
				LOG.info("Save {}", discogs);
			}
			
			discogs.setTitle(StringUtils.left(discogs.getTitle(),  MAX_LENGTH_DEFAULT));

			fillAtributes();
				
			save(discogs);
			
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
		discogs.setArtists(getArtists(discogs.getArtistIds()));
		
		for (Track t : ((Release) discogs).getTracklist()) {
			t.setArtists(getArtists(t.getArtistIds()));
		}
		
		((Release) discogs).setMaster(em.find(Master.class, ((Release) discogs).getMasterId()));
	}
	
	private Set<Artist> getArtists(List<Long> artistIds) {
		Set<Artist> result = new HashSet<>();
		
		for (long l : artistIds) {
			Artist a0 = em.find(Artist.class, l);
		
			if (a0 == null) {
				LOG.debug("Artist {} not found", l);
			} else {
				result.add(a0);
			}
		}
		
		return result;
	}
}
