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
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.tgmz.discogs.database.DatabaseService;
import jakarta.persistence.EntityManager;

public class DiscogsContentHandler extends DefaultHandler {
	protected static final Pattern PA = Pattern.compile("^(.*)(\\s?\\(\\d+\\))$");
	protected static final Logger LOG = LoggerFactory.getLogger(DiscogsContentHandler.class);
	protected static final int MAX_LENGTH_DEFAULT = 254;
	protected static final int MAX_LENGTH_DISPLAY = 510;
	private Deque<String> stack;
	protected String path;
	protected EntityManager em;
	protected XMLReader xmlReader;
	protected int count;
	/** For use in filtered handlers to finetune logging */
	protected long threshold = 10_000L;
	private StringBuilder chars;

	public DiscogsContentHandler() {
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
	
	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startDocument() throws SAXException {
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
		
		stack = new LinkedList<>();
		path = "";
	}
	
	@Override
	public void endDocument() throws SAXException {
		em.close();
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
		switch (stack.peek()) {
		// Ignore irrelevant sections
		case "notes", "description":
			return;
		default:
			chars.append(String.valueOf(Arrays.copyOfRange(ch, start, start + length)));
		}
	}

	public void save(Object o) {
		LOG.debug("Save {}", o);
		
		DatabaseService.getInstance().inTransaction(x -> x.merge(o));
		
		++count;
	}

	public String getChars() {
		// Remove superflous blanks
		return chars.toString().trim().replaceAll("\\s{2,}", " ");
	}
	
	public String getChars(int maxLength) {
		return StringUtils.left(getChars(), maxLength);
	}
	
	private void popStack() {
		stack.pop();
		path = stack.reversed().toString();
	}
}
