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

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.load.persist.LabelPersistable;

public class LabelContentHandler extends DiscogsContentHandler {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(LabelContentHandler.class);
	private Label label;
	private Predicate<Label> filter;

	public LabelContentHandler() {
		this (l -> true);
	}

	public LabelContentHandler(Predicate<Label> filter) {
		this.filter = filter;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		persister = new LabelPersistable(filter);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);

		switch (path) {
		case "[labels, label]":
			label = new Label();
			break;
		case "[labels, label, parentLabel]":
			Label pl = new Label();
			pl.setId(Long.parseLong(attributes.getValue("id")));
			
			label.setParentLabel(pl);
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[labels, label, id]":
			label.setId(Long.parseLong(getChars()));
			break;
		case "[labels, label, name]":
			label.setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[labels, label, data_quality]":
			label.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[labels, label, parentLabel]":
			label.getParentLabel().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[labels, label]":
			save(label);
		
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
}
