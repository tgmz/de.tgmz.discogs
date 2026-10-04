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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.load.factory.PersisterFactory;

public class MasterContentHandler extends DiscogsContentHandler {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(MasterContentHandler.class);
	private int artistId;
	private String artistName;
	private Master master;
	private List<String> artistNames;
	private List<String> joins;

	public MasterContentHandler() {
		this (x -> true);
	}
	
	public MasterContentHandler(Predicate<Master> filter) {
		persister = PersisterFactory.getInstance().create(Master.class, filter);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[masters, master]":
			master = new Master();
			
			this.id = Integer.parseInt(attributes.getValue("id"));

			master.setId(id);
			
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
			artistId = Integer.parseInt(getChars());
			
			break;
		case "[masters, master, title]":
			master.setTitle(getChars());
			
			break;
		case "[masters, master, year]":
			master.setPublished(Integer.parseInt(getChars()));
			
			break;
		case "[masters, master, data_quality]":
			master.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[masters, master, genres, genre]":
			master.getGenres().add(new Genre(getChars()));
			
			break;
		case "[masters, master, styles, style]":
			master.getStyles().add(new Style(getChars()));
			
			break;
		case "[masters, master, artists]":
			master.setAlbumArtist(computeBand(artistNames, joins));
			
			break;
		case "[masters, master, artists, artist, name]":
			artistName = getChars(true);
			
			artistNames.add(artistName);
			
			break;
		case "[masters, master, artists, artist]":
			Artist a = new Artist(artistId);
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
