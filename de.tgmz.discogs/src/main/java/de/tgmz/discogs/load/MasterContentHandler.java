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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.load.factory.GenreFactory;
import de.tgmz.discogs.load.factory.StyleFactory;
import de.tgmz.discogs.load.persist.MasterPersistable;

public class MasterContentHandler extends DiscogsContentHandler {
	private long artistId;
	private String artistName;
	private Master master;
	private List<String> artistNames;
	private List<String> joins;
	private Predicate<Master> filter;
	private GenreFactory genreFactory;
	private StyleFactory styleFactory;

	public MasterContentHandler() {
		this (x -> true);
	}
	
	public MasterContentHandler(Predicate<Master> filter) {
		this.filter = filter;
		
		genreFactory = new GenreFactory();
		styleFactory = new StyleFactory();
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		persister = new MasterPersistable(filter);
	}
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[masters, master]":
			master = new Master();
			
			master.setId(Long.parseLong(attributes.getValue("id")));
			
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
			master.setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[masters, master, year]":
			master.setPublished(Integer.parseInt(getChars()));
			
			break;
		case "[masters, master, data_quality]":
			master.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[masters, master, genres, genre]":
			master.getGenres().add(genreFactory.get(getChars()));
			
			break;
		case "[masters, master, styles, style]":
			master.getStyles().add(styleFactory.get(getChars()));
			
			break;
		case "[masters, master, artists]":
			master.setDisplayArtist(getDisplayArtist(artistNames, joins));
			
			break;
		case "[masters, master, artists, artist, name]":
			artistName = getChars(MAX_LENGTH_DEFAULT, true);
			
			artistNames.add(artistName);
			
			break;
		case "[masters, master, artists, artist]":
			Artist a = new Artist();
			
			a.setId(artistId);
			a.setName(artistName);
			
			master.getArtists().add(a);
			
			break;
		case "[masters, master, artists, artist, join]":
			joins.add(getChars());
			
			break;
		case "[masters, master, artists, artist, anv]":
			artistNames.set(artistNames.size() - 1, getChars());
			
			break;
		case "[masters, master]":
			save(master);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
}
