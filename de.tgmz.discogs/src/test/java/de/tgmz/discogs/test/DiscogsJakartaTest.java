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
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.DiscogsContentHandler;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.mp3.discogs.load.predicate.DataQualityFilter;
import de.tgmz.mp3.discogs.load.predicate.IgnoreUpToFilter;
import de.tgmz.mp3.discogs.load.predicate.MainFilter;

public class DiscogsJakartaTest extends DiscogsTest {
	@BeforeClass
	public static void setupOnce() throws IOException {
		DiscogsTest.setupOnce();
		
		DiscogsContentHandler dch;
		
		dch = new ArtistContentHandler();
		extractAndProcess("discogs_artists.xml.gz", dch);
		
		dch = new LabelContentHandler();
		extractAndProcess("discogs_labels.xml.gz", dch);
		
		dch = new MasterContentHandler(x -> x.getId() != 115L);
		extractAndProcess("discogs_masters.xml.gz", dch);
		
		Predicate<Release> p0 = new IgnoreUpToFilter();
		Predicate<Release> p1 = new MainFilter();
		Predicate<Release> p2 = new DataQualityFilter(DataQuality.values());
		Predicate<Release> p3 = new IgnoreUpToFilter(1);
		
		Predicate<Release> p = p0.or(p1).or(p2).or(p3);
		
		dch = new ReleaseContentHandler(p);
		dch.setSaveThreshold(2);
		extractAndProcess("discogs_releases.xml.gz", dch);

		// Force second load to check if updates work
		dch = new ReleaseContentHandler();
		extractAndProcess("discogs_releases.xml.gz", dch);
	}
	
	@AfterClass
	public static void teardownOnce() throws IOException {
		DiscogsTest.teardownOnce();
	}
}
