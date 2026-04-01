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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
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
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

public class DiscogsContentHandler extends DefaultHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsContentHandler.class);
	private static final Pattern PA = Pattern.compile("^(.*)(\\s?\\(\\d+\\))$");
	private static final int DEFAULT_LENGTH = 255;
	private Map<String, Integer> pathMap = new HashMap<>();
	private Set<EntityType<?>> entities = new HashSet<>();
	private Deque<String> stack;
	private XMLReader xmlReader;
	private int saved;
	private int count;
	private long logThreshold = 10_000L;
	private int saveThreshold = 100;
	private StringBuilder chars;
	private DBDefrag defrag;
	private BiPredicate<Integer, Integer> defragThreshold = (c,s) -> false;
	protected String path;
	@SuppressWarnings("rawtypes")
	protected IPersistable persister;

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
		
		defrag = new DBDefrag();

		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			entities = em.getMetamodel().getEntities();
		}
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
		saved += persister.flush();
		
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
			
			sb.append(s0);

			if (",".equals(s1)) {
				sb.append(", ");
			} else {
				sb.append(" ");
				sb.append(s1);
				sb.append(" ");
			}
		}
		
		String band = Strings.CS.removeEnd(sb.toString(), ", ").trim().replace(" , ", ", ");
		
		return StringUtils.left(band, 511);
	}
	
	@SuppressWarnings("unchecked")
	public void save(Object o) {
		saved += persister.save(saveThreshold, o);
		
		if (++count % logThreshold == 0 && LOG.isInfoEnabled()) {
			LOG.info("{}/{} ({}). {}", String.format("%,d", saved), String.format("%,d", count), String.format("%f%%", (float) saved / count * 100), o);
		}
		
		if (defragThreshold.test(count, saved)) {
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
		
		return StringUtils.left(s.strip(), pathMap.computeIfAbsent(path, i -> computeColumnLength(path)));
	}
	
	public String getChars() {
		return getChars(false);
	}
	
	private void popStack() {
		stack.pop();
		path = stack.reversed().toString();
	}

	public void setDefragThreshold(int defragThreshold) {
		this.defragThreshold = (c,s) -> c % defragThreshold == defragThreshold - 1 && s > 0;
	}

	public void setDefragThreshold(BiPredicate<Integer, Integer> defragThreshold) {
		this.defragThreshold = defragThreshold;
	}

	public void setSaveThreshold(int saveThreshold) {
		this.saveThreshold = saveThreshold;
	}
	
	private int computeColumnLength(String path) {
		String[] p0 =  StringUtils.split(StringUtils.substringBetween(path, "[", "]"), ", ");
		
		int last = p0.length - 1;
				
		if (p0.length > 1) {
			// We consider the last entry as the attributes name and 
			// iterate over its predecessors, considering them as types
			for (int i = last - 1; i > -1; i--) {
				Integer ccl = computeColumnLength(p0[i], p0[last]);
				
				if (ccl != null) {
					return ccl.intValue();
				}
			}
		}
		
		LOG.debug("Cannot compute column length for path {}", path);
		
		return DEFAULT_LENGTH;
	}
	
	private Integer computeColumnLength(String entity, String attribute) {
		Optional<EntityType<?>> oet = entities.stream().filter(et -> et.getName().equalsIgnoreCase(entity)).findFirst();
			
		if (oet.isPresent()) {
			try {
				Member m = oet.get().getAttribute(attribute).getJavaMember();
				
				if (m instanceof Field f) {
					Column a = f.getAnnotation(Column.class);
					
					if (a != null) {
						return a.length();
					} else {
						return f.getType() == String.class ? DEFAULT_LENGTH : Integer.MAX_VALUE;
					}
				}
			} catch (IllegalArgumentException e) {
				// If not found, ignore
			}
		}
		
		return null;
	}
}
