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

import java.util.TreeSet;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;

public class ArtistContentHandler extends DiscogsContentHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ArtistContentHandler.class);
	private Artist artist;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);

		switch (path) {
		case "[artist, artists]":
			artist = new Artist();
			break;
		case "[namevariations, artist, artists]":
			artist.setVariations(new TreeSet<>());
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[id, artist, artists]":
			artist.setId(Long.parseLong(getChars()));
			break;
		case "[name, artist, artists]":
			String s = getChars();
			
			Matcher m = PA.matcher(s);
			
			if (m.matches() && m.groupCount() > 1) {
				s = m.group(1);
			}
			
			artist.setName(s.trim());
			
			break;
		case "[name, namevariations, artist, artists]":
			artist.getVariations().add(getChars());
				
			break;
		case "[artist, artists]":
			if (artist.getId() % 10_000 == 0) {
				LOG.info("Save {}", artist);
			}
			
			save(artist);
		
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isInfoEnabled()) {
			LOG.info("{} artists inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
}
