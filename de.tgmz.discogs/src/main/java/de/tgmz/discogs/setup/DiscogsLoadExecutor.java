/*********************************************************************
* Copyright (c) 06.06.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.setup;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.tgmz.discogs.load.FilteredContentHandler;

public class DiscogsLoadExecutor implements Callable<Boolean> {
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsLoadExecutor.class);
	private FilteredContentHandler fch;
	private InputStream is;

	public DiscogsLoadExecutor(FilteredContentHandler fch, InputStream is) {
		super();
		this.fch = fch;
		this.is = is;
	}
	
	@Override
	public Boolean call() {
		try {
			fch.run(is);
		} catch (IOException | SAXException e) {
			LOG.error("Error running ContentHandler", e);
			
			return false;
		}
		
		return true;
	}

}
