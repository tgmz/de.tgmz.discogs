/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.setup;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.sonar.discogs.generated.ListBucketResult;
import de.tgmz.sonar.discogs.generated.ListBucketResult.Contents;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Factory for creating the ListBucketResult from amazonws.
 */
public final class LbrFactory {
	private static final Logger LOG = LoggerFactory.getLogger(LbrFactory.class);
	private static final LbrFactory INSTANCE = new LbrFactory();
	private ListBucketResult lbr;

	/**
	 * Private constructor for security reasons
	 */
	private LbrFactory() {
		String uri = System.getProperty(DiscogsFile.DISCOGS_URL, "https://discogs-data-dumps.s3-us-west-2.amazonaws.com/");
		
		LOG.info("Create ListBucketResult from {}", uri);
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ListBucketResult.class);

			lbr = (ListBucketResult) jaxbContext.createUnmarshaller().unmarshal(URI.create(uri).toURL());
		} catch (MalformedURLException | JAXBException e) {
			throw new DiscogsSetupException(e);
		}
	}

	public static LbrFactory getInstance() {
		return INSTANCE;
	}

	public Contents getContents(String p) {
		return lbr.getContents().stream()
				.filter(c -> c.getKey().endsWith(p))	// Filter contents ending on key (e.g. "artists.xml.gz")
				.max((c0, c1) -> c0.getLastModified().compare(c1.getLastModified()))	// Get contents with maximum lastModified
				.orElseThrow(NoSuchElementException::new);	// Failsafe
	}
}
