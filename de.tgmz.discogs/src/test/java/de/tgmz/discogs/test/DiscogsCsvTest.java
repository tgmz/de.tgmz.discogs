/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.test;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.CsvLoader;
import de.tgmz.discogs.load.DiscogsContentHandler;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.discogs.load.persist.csv.ArtistCsvPersister;
import de.tgmz.discogs.load.persist.csv.LabelCsvPersister;
import de.tgmz.discogs.load.persist.csv.MasterCsvPersister;
import de.tgmz.discogs.load.persist.csv.ReleaseCsvPersister;

public class DiscogsCsvTest extends DiscogsTest {
	@BeforeClass
	public static void setupOnce() throws IOException {
		DiscogsTest.setupOnce();
		
		DiscogsContentHandler dch;
		
		dch = new ArtistContentHandler();
		dch.setPersister(new ArtistCsvPersister(dataDir.toString()));
		extractAndProcess("discogs_artists.xml.gz", dch);
		
		dch = new LabelContentHandler();
		dch.setPersister(new LabelCsvPersister(dataDir.toString()));
		extractAndProcess("discogs_labels.xml.gz", dch);
		
		dch = new MasterContentHandler();
		dch.setPersister(new MasterCsvPersister(dataDir.toString()));
		extractAndProcess("discogs_masters.xml.gz", dch);
		
		dch = new ReleaseContentHandler();
		dch.setPersister(new ReleaseCsvPersister(dataDir.toString()));
		extractAndProcess("discogs_releases.xml.gz", dch);

		CsvLoader csvl = new CsvLoader(dataDir.toString());
		
		csvl.load();
	}
	
	@AfterClass
	public static void teardownOnce() throws IOException {
		DiscogsTest.teardownOnce();
	}
}
