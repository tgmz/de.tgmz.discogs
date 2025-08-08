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

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.load.persist.ArtistPersistable;

public class ArtistContentHandler extends DiscogsContentHandler {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ArtistContentHandler.class);
	private Artist artist;
	private Artist alias;
	private Artist group;
	private Artist member;
	private Predicate<Artist> filter;

	public ArtistContentHandler() {
		this(x -> true);
	}

	public ArtistContentHandler(Predicate<Artist> filter) {
		super();
		
		this.filter = filter;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		persister = new ArtistPersistable(filter);
	}
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);

		switch (path) {
		case "[artists, artist]":
			artist = new Artist();
			break;
		case "[artists, artist, members, name]":
			member = new Artist(Long.parseLong(attributes.getValue("id")));
			break;
		case "[artists, artist, aliases, name]":
			alias = new Artist(Long.parseLong(attributes.getValue("id")));
			break;
		case "[artists, artist, groups, name]":
			group = new Artist(Long.parseLong(attributes.getValue("id")));
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[artists, artist, id]":
			artist.setId(Long.parseLong(getChars()));
			
			break;
		case "[artists, artist, data_quality]":
			artist.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[artists, artist, name]":
			artist.setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[artists, artist, realname]":
			artist.setRealName(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[artists, artist, namevariations, name]":
			artist.getVariations().add(getChars());
				
			break;
		case "[artists, artist, members, name]":
			member.setName(getChars(MAX_LENGTH_DEFAULT, true));
			artist.getMembers().add(member);
			
			break;
		case "[artists, artist, aliases, name]":
			alias.setName(getChars(MAX_LENGTH_DEFAULT, true));
			artist.getAliases().add(alias);
			
			break;
		case "[artists, artist, groups, name]":
			group.setName(getChars(MAX_LENGTH_DEFAULT, true));
			artist.getGroups().add(group);
			
			break;
		case "[artists, artist]":
			save(artist);
		
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
}
