/*********************************************************************
* Copyright (c) 18.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Label;

public class LabelContentHandler extends DiscogsContentHandler {
	private static final Logger LOG = LoggerFactory.getLogger(LabelContentHandler.class);
	private Label label;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);

		if (path.equals("[labels, label]")) {
			label = new Label();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[labels, label, id]":
			label.setId(Long.parseLong(getChars()));
			break;
		case "[labels, label, name]":
			String s = getChars(MAX_LENGTH_DEFAULT);
			
			Matcher m = PA.matcher(s);
			
			if (m.matches() && m.groupCount() > 1) {
				s = m.group(1);
			}
			
			label.setName(s.trim());
			
			break;
		case "[labels, label, data_quality]":
			label.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[labels, label]":
			if (label.getId() % 10_000 == 0) {
				LOG.info("Save {}", label);
			}
			
			save(label);
		
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	@Override
	public void endDocument() throws SAXException {
		if (LOG.isInfoEnabled()) {
			LOG.info("{} labels inserted/updated", String.format("%,d", count));
		}
		
		super.endDocument();
	}
}
