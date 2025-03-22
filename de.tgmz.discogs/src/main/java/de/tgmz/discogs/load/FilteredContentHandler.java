/*********************************************************************
* Copyright (c) 20.03.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Style;

public class FilteredContentHandler extends DiscogsContentHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(FilteredContentHandler.class);
	protected List<Predicate<Discogs>> filter;
	protected Discogs discogs;
	protected int ignored;
	private Set<String> genres;
	private Set<String> styles;

	public FilteredContentHandler() {
		this (List.of(x -> true));
	}
	
	public FilteredContentHandler(List<Predicate<Discogs>> filter) {
		this.filter = filter;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName) {
		case "genre":
			genres = new TreeSet<>();
		
			break;
		case "style":
			styles = new TreeSet<>();
		
			break;
		default:
		}
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		
		switch (qName) {
		case "genre":
			genres.add(getChars());
		
			break;
		case "style":
			styles.add(getChars());
		
			break;
		default:
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	
		if (LOG.isInfoEnabled()) {
			LOG.info("{} ignored", String.format("%,d", ignored));
		}
	}

	protected String getDisplayArtist(List<String> artists, List<String> joins) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < artists.size(); ++i) {
			String s0 = artists.get(i);
			String s1 = i < joins.size() ? joins.get(i) : ",";
			
			Matcher m = PA.matcher(s0);
			
			if (m.matches() && m.groupCount() > 1) {
				s0 = m.group(1);
			}
			
			sb.append(s0 + (",".equals(s1) ? ", " : " " + s1 +" "));
		}
		
		String display = StringUtils.removeEnd(sb.toString(), ", ").trim().replace(" , ", ", ");
		
		return StringUtils.left(display, MAX_LENGTH_DISPLAY);
	}
	
	@Override
	public void save(Object o) {
		if (filter.stream().anyMatch(x -> x.test(discogs))) {
			for (String x : genres) {
				Genre g = em.find(Genre.class, x);
				
				if (g != null) {
					discogs.getGenres().add(g);
				} else {
					discogs.getGenres().add(new Genre(x));
				}
			}

			for (String x : styles) {
				Style s = em.find(Style.class, x);
				
				if (s != null) {
					discogs.getStyles().add(s);
				} else {
					discogs.getStyles().add(new Style(x));
				}
			}
			
			super.save(o);
		} else {
			LOG.debug("Ignore {}", o);
			
			++ignored;
		}
	}
}
