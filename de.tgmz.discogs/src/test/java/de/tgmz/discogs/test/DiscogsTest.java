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
import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.discogs.logging.LogUtil;
import jakarta.persistence.EntityManager;

public class DiscogsTest {
	private static EntityManager em;
	@BeforeClass
	public static void setupOnce() {
		System.setProperty("DB_URL", "jdbc:h2:mem:discogs_test_mem");
		System.setProperty("DB_USR", "sa");
		System.setProperty("DB_PASS", "sa");
		
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
	}
	
	@AfterClass
	public static void teardownOnce() {
		em.close();
		
		LogUtil.logElapsed();
	}
	
	@Test
	public void test() throws IOException, SAXException {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_artists_Depeche_Mode.xml")) {
			new ArtistContentHandler().run(is);
		}
		
		Artist a = em.find(Artist.class, 2725L);
		
		assertEquals("Depeche Mode", a.getName());
		
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_masters_Violator.xml")) {
			new MasterContentHandler().run(is);
		}
		
		Master m = em.find(Master.class, 18080L);

		assertEquals("Violator", m.getTitle());
		
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_releases_Violator.xml")) {
			new ReleaseContentHandler().run(is);
		}
		
		Release r = em.find(Release.class, 10222L);

		assertEquals("World In My Eyes", r.getTracklist().getFirst().getTitle());
		assertEquals(m, r.getMaster());
		assertEquals("US", r.getCountry());
		assertEquals("1990-03-20", r.getReleased());
		assertEquals("Correct", r.getDataQuality());
	}
}
