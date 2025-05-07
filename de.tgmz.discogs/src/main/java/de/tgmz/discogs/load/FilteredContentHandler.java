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

public abstract class FilteredContentHandler extends DiscogsContentHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(FilteredContentHandler.class);
	protected Discogs discogs;
	protected int saved;
	protected int ignored;
	private Predicate<Discogs> filter;
	private Set<String> genres;
	private Set<String> styles;

	protected FilteredContentHandler() {
		this (x -> true);
	}
	
	protected FilteredContentHandler(Predicate<Discogs> filter) {
		this.filter = filter;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName) {
		case "master", "release":
			genres = new TreeSet<>();
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
			LOG.info("{} saved  ", String.format("%,d", saved));
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
		if (discogs.getId() % threshold == 0 && LOG.isInfoEnabled()) {
			LOG.info("{}/{} ({}). {}", String.format("%,d", saved), String.format("%,d", ignored), String.format("%f%%", (float) saved / (ignored + saved) * 100), discogs);
		}
		
		if (filter.test(discogs)) {
			fillGenresAndStyles();
			
			fillAttributes(discogs);
			
			super.save(o);
			
			++saved;
		} else {
			LOG.debug("Ignore {}", o);
			
			++ignored;
		}
	}

	private void fillGenresAndStyles() {
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
	}
	
	/**
	 * By implementing this method derived classes may avoid time-intensive computations of attributes as this
	 * method is called <i>after</i> applying the filter(s). 
	 * @param d
	 */
	protected abstract void fillAttributes(Discogs d);
}
