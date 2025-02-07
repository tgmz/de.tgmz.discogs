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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Master;

public class MasterContentHandler extends DiscogsContentHandler {
	private Artist artist;
	private List<String> artistNames;
	private List<String> joins;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[master, masters]":
			discogs = new Master();
			
			((Master) discogs).setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case "[artists, master, masters]":
			discogs.setArtists(new LinkedList<>());
			
			artistNames = new ArrayList<>();
			joins = new ArrayList<>();
			
			break;
		case "[artist, artists, master, masters]":
			artist = new Artist();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[id, artist, artists, master, masters]":
			long id = Long.parseLong(getChars());
			
			artist.setId(id);
			
			break;
		case "[name, artist, artists, master, masters]":
			artistNames.add(getChars());
			
			break;
		case "[anv, artist, artists, master, masters]":
			artistNames.set(artistNames.size() - 1, getChars());
			
			break;
		case "[artist, artists, master, masters]":
			Artist a0 = em.find(Artist.class, artist.getId());
			
			if (a0 == null) {
				LOG.debug("Artist {} not found", artist.getId());
			} else {
				discogs.getArtists().add(a0);
			}
			
			break;
			
		case "[year, master, masters]":
			((Master) discogs).setPublished(Integer.parseInt(getChars()));
			
			break;
			
		case "[join, artist, artists, master, masters]":
			joins.add(getChars());
			
			break;

		case "[data_quality, master, masters]":
			discogs.setDataQuality(getChars());
			
			break;

		case "[artists, master, masters]":
			discogs.setDisplayArtist(getDisplayArtist(artistNames, joins));
			
			break;
			
		case "[title, master, masters]":
			discogs.setTitle(getChars());
			
			break;
		case "[master, masters]":
			if (((Master) discogs).getId() % threshold == 0) {
				LOG.info("Save {}", discogs);
			}
			
			discogs.setTitle(StringUtils.left(discogs.getTitle(),  MAX_LENGTH_DEFAULT));
			
			save(discogs);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isInfoEnabled()) {
			LOG.info("{} masters inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
	
	/**
	 * For use in filtered handlers
	 * @return the master
	 */
	protected Master getMaster() {
		return (Master) discogs;
	}
}
