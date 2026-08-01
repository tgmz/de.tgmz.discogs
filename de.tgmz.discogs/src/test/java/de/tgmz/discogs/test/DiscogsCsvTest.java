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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.load.Action;
import de.tgmz.discogs.load.ActionFactory;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.DatabaseAction;
import de.tgmz.discogs.load.DiscogsContentHandler;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.Mode;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.discogs.load.persist.csv.ArtistCsvPersister;
import de.tgmz.discogs.load.persist.csv.LabelCsvPersister;
import de.tgmz.discogs.load.persist.csv.MasterCsvPersister;
import de.tgmz.discogs.load.persist.csv.ReleaseCsvPersister;
import de.tgmz.discogs.load.persist.csv.Table;

public class DiscogsCsvTest extends DiscogsTest {
	@BeforeClass
	public static void setupOnce() throws IOException {
		DiscogsTest.setupOnce();
		
		init();
		
		ActionFactory af = ActionFactory.getInstance().forTables(dataDir, Table.values());

		List<DatabaseAction> l = new LinkedList<>(af.create(Action.LOAD, Mode.PARALLEL));
		l.addAll(af.create(Action.RECONCILE));
		l.addAll(af.create(Action.INDEX));
		l.addAll(af.create(Action.CONSTRAINT));
		
		for (Table t : Table.values()) {
			l.stream().filter(da -> da.getTable() == t).forEach(da -> da.compute());
		}
	}
	
	@AfterClass
	public static void teardownOnce() throws IOException {
		DiscogsContentHandler dch = new ReleaseContentHandler();
		
		dch.setSaveThreshold(1);
		extractAndProcess("discogs_releases.xml.gz", dch);
		DiscogsTest.teardownOnce();
	}
	
	@Test
	public void testMissingArtists() {
		assertEquals("Unknown Artist", em.find(Artist.class, 355).getName());
		assertEquals("No Artist", em.find(Artist.class, 118760).getName());
	}

	protected static void init() throws IOException {
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
	}
}
