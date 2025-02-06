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

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Style;
import jakarta.persistence.EntityManager;

public class DiscogsContentHandler extends DefaultHandler {
	protected static final Pattern PA = Pattern.compile("^(.*)(\\s+\\(\\d+\\))$");
	protected static final Logger LOG = LoggerFactory.getLogger(DiscogsContentHandler.class);
	protected static final int MAX_LENGTH_DEFAULT = 254;
	protected static final int MAX_LENGTH_DISPLAY = 510;
	protected Deque<String> stack;
	protected EntityManager em;
	protected XMLReader xmlReader;
	protected Discogs discogs;
	protected int count;
	private StringBuilder chars;

	public DiscogsContentHandler() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			
			SAXParser saxParser = spf.newSAXParser();

			xmlReader = saxParser.getXMLReader();
		} catch (ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void startDocument() throws SAXException {
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
		
		stack = new LinkedList<>();
	}
	
	@Override
	public void endDocument() throws SAXException {
		em.close();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		stack.push(qName);
		
		chars = new StringBuilder();
		
		switch (qName) {
		case "genres":
			discogs.setGenres(new HashSet<>());

			break;
		case "styles":
			discogs.setStyles(new HashSet<>());

			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		stack.pop();

		switch (qName) {
		case "genre":
			discogs.getGenreNames().add(getChars());
		
			break;
		case "style":
			discogs.getStyleNames().add(getChars());
		
			break;
		default:
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (!"notes".equals(stack.peek())) {
			chars.append(String.valueOf(Arrays.copyOfRange(ch, start, start + length)));
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

	public void save(Object o) {
		LOG.debug("Save {}", o);

		if (o instanceof Discogs d) {
			if (!d.getGenreNames().isEmpty()) {
				Set<Genre> gs = new HashSet<>();
			
				for (String s : d.getGenreNames()) {
					List<Genre> gl = em.createNamedQuery("Genre.getByName", Genre.class).setParameter(1, s).getResultList();
				
					if (!gl.isEmpty()) {
						gs.add(gl.getFirst());
					} else {
						gs.add(new Genre(s));
					}
				}
				
				d.setGenres(gs);
			}
			
			if (!d.getStyleNames().isEmpty()) {
				Set<Style> ss = new HashSet<>();
			
				for (String s : d.getStyleNames()) {
					List<Style> sl = em.createNamedQuery("Style.getByName", Style.class).setParameter(1, s).getResultList();
				
					if (!sl.isEmpty()) {
						ss.add(sl.getFirst());
					} else {
						ss.add(new Style(s));
					}
				}
				
				d.setStyles(ss);
			}
		}


		DatabaseService.getInstance().inTransaction(x -> x.merge(o));
		
		++count;
	}

	public String getChars() {
		// Remove superflous blanks
		return chars.toString().trim().replaceAll("\\s{2,}", "\\s");
	}
}
