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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Master;

public class MasterContentHandler extends DiscogsContentHandler {
	private static final String TAG_MASTER = "master";
	private static final String TAG_ARTISTS = "artists";
	private static final String TAG_ARTIST = "artist";
	private Artist artist;
	private List<String> artists;
	private List<String> joins;

	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (qName) {
		case TAG_MASTER:
			discogs = new Master();
			
			((Master) discogs).setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case TAG_ARTISTS:
			discogs.setArtists(new HashSet<>());
			
			artists = new ArrayList<>();
			joins = new ArrayList<>();
			
			break;
		case TAG_ARTIST:
			artist = new Artist();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		
		switch (qName) {
		case "id":
			long id = Long.parseLong(getChars());
			
			artist.setId(id);
			
			break;
		case "name":
			artists.add(getChars());
			
			break;
		case "anv":
			artists.set(artists.size() - 1, getChars());
			
			break;
		case TAG_ARTIST:
			Artist a0 = em.find(Artist.class, artist.getId());
			
			if (a0 == null) {
				LOG.debug("Artist {} not found", artist.getId());
			} else {
				discogs.getArtists().add(a0);
			}
			
			break;
			
		case "year":
			((Master) discogs).setPublished(Integer.parseInt(getChars()));
			
			break;
			
		case "join":
			joins.add(getChars());
			
			break;

		case "data_quality":
			discogs.setDataQuality(getChars());
			
			break;

		case TAG_ARTISTS:
			discogs.setDisplayArtist(getDisplayArtist(artists, joins));
			
			break;
			
		case "title":
			if (stack.size() == 2) {
				discogs.setTitle(getChars());
			}
			
			break;
		case TAG_MASTER:
			if (((Master) discogs).getId() % 10_000 == 0 && LOG.isInfoEnabled()) {
				LOG.info("Save {}", discogs);
			}
			
			discogs.setTitle(StringUtils.left(discogs.getTitle(),  MAX_LENGTH_DEFAULT));
			
			save(discogs);
			
			break;
		default:
		}
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isInfoEnabled()) {
			LOG.info("{} masters inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
	
	public Master getMaster() {
		return (Master) discogs;
	}
}
