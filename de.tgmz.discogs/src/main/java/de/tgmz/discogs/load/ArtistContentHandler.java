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
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;

public class ArtistContentHandler extends DiscogsContentHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ArtistContentHandler.class);
	private static final String TAG_ARTIST = "artist";
	private Artist artist;

	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);

		if (TAG_ARTIST.equals(qName)) {
			artist = new Artist();
		}
		
		if ("namevariations".equals(qName)) {
			artist.setVariations(new TreeSet<>());
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		
		switch (qName) {
		case "id":
			artist.setId(Long.parseLong(getChars()));
			
			break;

		case "name":
			switch (stack.peek()) {
			case TAG_ARTIST:
				String s = getChars();
				
				Matcher m = PA.matcher(s);
				
				if (m.matches() && m.groupCount() > 1) {
					s = m.group(1);
				}
				
				artist.setName(s.trim());
				
				break;
			case "namevariations":
				artist.getVariations().add(getChars());
				
				break;
			default:
			}
			
			break;
		
		case TAG_ARTIST:
			if (artist.getId() % 10_000 == 0) {
				LOG.info("Save {}", artist);
			}
			
			save(artist);
		
			break;
		default:
		}
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isInfoEnabled()) {
			LOG.info("{} artists inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
}
