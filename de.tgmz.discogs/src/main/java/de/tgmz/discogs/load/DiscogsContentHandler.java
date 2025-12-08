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

import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.load.persist.IPersistable;
import jakarta.persistence.PersistenceException;

public class DiscogsContentHandler extends DefaultHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsContentHandler.class);
	private static final Pattern PA = Pattern.compile("^(.*)(\\s?\\(\\d+\\))$");
	private Deque<String> stack;
	private XMLReader xmlReader;
	private int saved;
	private int count;
	private long logThreshold = 10_000L;
	private StringBuilder chars;
	private DBDefrag defrag;
	private int defragThreshold = Integer.MAX_VALUE;
	protected static final int MAX_LENGTH_DEFAULT = 254;
	protected static final int MAX_LENGTH_LONG = 510;
	protected String path;
	@SuppressWarnings("rawtypes")
	protected IPersistable persister;

	public DiscogsContentHandler() {
		defrag = new DBDefrag();
		
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(false);
			spf.setValidating(false);
			
			SAXParser saxParser = spf.newSAXParser();

			xmlReader = saxParser.getXMLReader();
		} catch (ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void run(InputStream is) {
		xmlReader.setContentHandler(this);
		
		try {
			xmlReader.parse(new InputSource(is));
		} catch (IOException | SAXException e) {
			LOG.error("Parsing error", e);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		stack = new LinkedList<>();
		path = "";
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		stack.push(qName);
		path = stack.reversed().toString();
		
		chars = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		popStack();
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if ("notes".equals(stack.peek())) {
		// Ignore irrelevant sections
			return;
		}
		
		chars.append(String.valueOf(Arrays.copyOfRange(ch, start, start + length)));
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("{} entities added, {} ignored", String.format("%,d", saved), String.format("%,d", count - saved));
		}
		
		defrag.run();

		if (!Boolean.getBoolean("DISCOGS_TEST")) {	
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	protected String computeBand(List<String> artists, List<String> joins) {
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
		
		String band = Strings.CS.removeEnd(sb.toString(), ", ").trim().replace(" , ", ", ");
		
		return StringUtils.left(band, MAX_LENGTH_LONG);
	}
	
	@SuppressWarnings("unchecked")
	public void save(Object o) {
		try {
			DatabaseService.getInstance().inTransaction(x -> saved += persister.save(x, o));
		} catch (PersistenceException e) {
			LOG.error("Error storing {}", o, e);
			
			return;
		}
		
		if (++count % logThreshold == 0 && LOG.isInfoEnabled()) {
			LOG.info("{}/{} ({}). {}", String.format("%,d", saved), String.format("%,d", count), String.format("%f%%", (float) saved / count * 100), o);
		}
		
		if (count % defragThreshold == defragThreshold - 1 && saved > 0) {
			defrag.run();
		}
	}

	public String getChars(boolean removeSuffix) {
		// Remove superflous blanks
		String s = chars.toString().trim().replaceAll("\\s{2,}", " ");
		
		if (removeSuffix) {
			Matcher m = PA.matcher(s);
			
			if (m.matches() && m.groupCount() > 1) {
				s = m.group(1);
			}
		}
		
		return s.strip();
	}
	
	public String getChars(int maxLength, boolean removeSuffix) {
		return StringUtils.left(getChars(removeSuffix), maxLength);
	}
	
	public String getChars(int maxLength) {
		return getChars(maxLength, false);
	}
	
	public String getChars() {
		return getChars(MAX_LENGTH_DEFAULT, false);
	}
	
	private void popStack() {
		stack.pop();
		path = stack.reversed().toString();
	}

	public void setDefragThreshold(int defragThreshold) {
		this.defragThreshold = defragThreshold;
	}
}
