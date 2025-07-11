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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.Master;

public class MasterContentHandler extends FilteredContentHandler {
	private long artistId;
	private String artistName;
	private int artistCount = 0;
	private List<String> artistNames;
	private List<String> joins;

	public MasterContentHandler() {
		this(x -> true);
	}

	public MasterContentHandler(Predicate<Discogs> filter) {
		super(filter);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[masters, master]":
			discogs = new Master();
			
			discogs.setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case "[masters, master, artists]":
			artistNames = new ArrayList<>();
			joins = new ArrayList<>();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[masters, master, artists, artist, id]":
			artistId = Long.parseLong(getChars());
			
			break;
		case "[masters, master, title]":
			discogs.setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[masters, master, year]":
			((Master) discogs).setPublished(Integer.parseInt(getChars()));
			
			break;
		case "[masters, master, data_quality]":
			discogs.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[masters, master, artists]":
			discogs.setDisplayArtist(getDisplayArtist(artistNames, joins));
			
			break;
		case "[masters, master, artists, artist, name]":
			artistName = getChars(MAX_LENGTH_DEFAULT, true);
			
			artistNames.add(artistName);
			
			break;
		case "[masters, master, artists, artist]":
			Artist a = em.find(Artist.class, artistId);
			
			if (a == null) {
				a = new Artist();
				a.setId(artistId);
				a.setName(artistName);
				
				save(a);
				
				++artistCount;
				
				LOG.debug("Added new artist {}", a);
			}
			
			discogs.getArtists().add(a);
			
			break;
		case "[masters, master, artists, artist, join]":
			joins.add(getChars());
			
			break;
		case "[masters, master, artists, artist, anv]":
			artistNames.set(artistNames.size() - 1, getChars());
			
			break;
		case "[masters, master]":
			save(discogs);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("{} artists added  ", String.format("%,d", artistCount));
		}
	}
	@Override
	protected void fillAttributes(Discogs d) {
		d.setTitle(StringUtils.left(discogs.getTitle(), MAX_LENGTH_DEFAULT));
	}
}
