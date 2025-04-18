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

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.Master;

public class MasterContentHandler extends FilteredContentHandler {
	private Artist artist;
	private List<String> artistNames;
	private List<String> joins;

	public MasterContentHandler() {
		super();
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
			
			((Master) discogs).setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case "[masters, master, artists]":
			artistNames = new ArrayList<>();
			joins = new ArrayList<>();
			
			break;
		case "[masters, master, artists, artist]":
			artist = new Artist();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[masters, master, artists, artist, id]":
			long id = Long.parseLong(getChars());
			
			artist.setId(id);
			
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
		case "[masters, master, artists, artist]":
			Artist a0 = em.find(Artist.class, artist.getId());
			
			if (a0 == null) {
				LOG.debug("Artist {} not found", artist.getId());
			} else {
				discogs.getArtists().add(a0);
			}
			
			break;
		case "[masters, master, artists, artist, name]":
			artistNames.add(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[masters, master, artists, artist, join]":
			joins.add(getChars());
			
			break;
		case "[masters, master, artists, artist, anv]":
			artistNames.set(artistNames.size() - 1, getChars());
			
			break;
		case "[masters, master]":
			if (((Master) discogs).getId() % threshold == 0) {
				LOG.info("Save {}", discogs);
			}
			
			save(discogs);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	
	@Override
	protected void fillAttributes(Discogs d) {
		d.setTitle(StringUtils.left(discogs.getTitle(), MAX_LENGTH_DEFAULT));
	}
}
