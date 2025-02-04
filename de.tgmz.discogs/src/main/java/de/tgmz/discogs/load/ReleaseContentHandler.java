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
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Track;

public class ReleaseContentHandler extends DiscogsContentHandler {
	private static final String TAG_RELEASE = "release";
	private static final String TAG_ARTISTS = "artists";
	private static final String TAG_ARTIST = "artist";
	private Artist artist;
	private Track track;
	private List<String> displayArtists;
	private List<String> displayJoins;
	private boolean inTrack;

	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		inTrack = false;
		complete = false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		if (complete) {
			return;
		}
		
		switch (qName) {
		case TAG_RELEASE:
			discogs = new Release();
			
			((Release) discogs).setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case TAG_ARTISTS:
			if (!inTrack) {
				displayArtists = new ArrayList<>();
				displayJoins = new ArrayList<>();
			}
			
			break;
		case "master_id":
			((Release) discogs).setMain(Boolean.valueOf(attributes.getValue("is_main_release")));

			break;
		case TAG_ARTIST:
			artist = new Artist();
			
			break;
		case "tracklist":
			inTrack = true;
			
			((Release) discogs).setTracklist(new LinkedList<>());
			
			break;
		case "track":
			track = new Track();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		
		if (complete && !TAG_RELEASE.equals(qName)) {
			return;
		}
		
		switch (qName) {
		case "id":
			if (TAG_ARTIST.equals(stack.peek())) {
				artist.setId(Long.valueOf(getChars()));
			}
			
			break;
		case "master_id":
			((Release) discogs).setMasterId(Long.parseLong(getChars()));

			break;
		case "name":
			if (!inTrack) {
				displayArtists.add(getChars());
			}
			
			break;
		case "anv":
			if (!inTrack) {
				displayArtists.set(displayArtists.size() - 1, getChars());
			}
			
			break;
		case TAG_ARTIST:
			if (!stack.contains("extraartists")) {
				if (inTrack) {
					track.getArtistIds().add(artist.getId());
				} else {
					discogs.getArtistIds().add(artist.getId());
				}
			}
			
			break;
		case "join":
			if (!inTrack) {
				displayJoins.add(getChars());
			}
			
			break;
		case "position":
			track.setPosition(getChars());
			
			break;
		case TAG_ARTISTS:
			if (!inTrack) {
				discogs.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			}
			
			break;
		case "duration":
			track.setDuration(getChars());
			
			break;
		case "title":
			switch (stack.peek()) {
			case TAG_RELEASE:
				if (discogs.getTitle() == null) {
					discogs.setTitle(getChars());
				}
				
				break;
			case "track":
				track.setTitle(StringUtils.left(getChars(), MAX_LENGTH_DEFAULT));
				
				((Release) discogs).getTracklist().add(track);
				
				break;
			default:
				break;
			}
				
			break;
		case "tracklist":
			complete = true;
			inTrack = false;
			
			break;
		case "released":
			((Release) discogs).setReleased(getChars());
			
			break;
		case "data_quality":
			discogs.setDataQuality(getChars());
			
			break;
		case "country":
			((Release) discogs).setCountry(getChars());
			
			break;
		case TAG_RELEASE:
			if (((Release) discogs).getId() % 10_000 == 0 && LOG.isInfoEnabled()) {
				LOG.info("Save {}", discogs);
			}
			
			discogs.setTitle(StringUtils.left(discogs.getTitle(),  MAX_LENGTH_DEFAULT));

			fillAtributes();
				
			save(discogs);
			
			complete = false;
			
			break;
		default:
		}
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
