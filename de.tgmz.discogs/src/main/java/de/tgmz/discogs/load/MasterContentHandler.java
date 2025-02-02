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
	private Master master;
	private Artist artist;
	private List<String> artists;
	private List<String> joins;
	private boolean complete;

	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

		complete = false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		if (complete) {
			return;
		}
		
		switch (qName) {
		case TAG_MASTER:
			master = new Master();
			
			master.setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case TAG_ARTISTS:
			master.setArtists(new HashSet<>());
			
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
		
		if (complete && !TAG_MASTER.equals(qName)) {
			return;
		}
		
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
				master.getArtists().add(a0);
			}
			
			break;
			
		case "year":
			master.setPublished(Integer.parseInt(getChars()));
			
			break;
			
		case "join":
			joins.add(getChars());
			
			break;

		case TAG_ARTISTS:
			master.setDisplayArtist(getDisplayArtist(artists, joins));
			
			break;
			
		case "title":
			if (stack.size() == 2) {
				master.setTitle(getChars());
				
				complete = true;
			}
			
			break;
		case TAG_MASTER:
			if (master.getId() % 10_000 == 0 && LOG.isInfoEnabled()) {
				LOG.info("Save {}", master);
			}
			
			master.setTitle(StringUtils.left(master.getTitle(),  MAX_LENGTH_TITLE));
			
			save(master);
			
			complete = false;
			
			break;
		default:
		}
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isErrorEnabled()) {
			LOG.info("{} masters inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
}
