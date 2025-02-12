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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Track;
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
	public void testViolator() throws IOException, SAXException {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_artists_Depeche_Mode.xml")) {
			new ArtistContentHandler().run(is);
		}
		
		Artist a = em.find(Artist.class, 2725L);
		checkArtist(a);
		
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_masters_Violator.xml")) {
			new MasterContentHandler().run(is);
		}
		
		Master m = em.find(Master.class, 18080L);
		checkMaster(m);

		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_releases_Violator.xml")) {
			new ReleaseContentHandler().run(is);
		}
		
		Release r = em.find(Release.class, 10222L);
		checkRelease(r, m);
	}
	@Test
	public void testSubtrack() throws IOException, SAXException {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs_20250101_r2460568.xml")) {
			new ReleaseContentHandler().run(is);
		}
		
		Release r = em.find(Release.class, 2460568L);
		assertTrue(r.getTracklist().get(10).getSubTracklist().isEmpty());
		assertEquals("Sometimes I Feel Like A Motherless Child", r.getTracklist().get(11).getSubTracklist().get(0).getTitle());
	}
	private void checkArtist(Artist a) {
		assertEquals("Depeche Mode", a.getName());
		assertTrue(a.getVariations().contains("D M"));
	}
	private void checkMaster(Master m) {
		assertEquals("Violator", m.getTitle());
		assertEquals("Correct", m.getDataQuality());
		assertEquals(1990, m.getPublished().intValue());
		assertTrue(m.getGenres().stream().allMatch(x -> x.getName().equals("Electronic")));
		assertTrue(m.getStyles().stream().allMatch(x -> x.getName().equals("Synth-pop")));
	}
	private void checkRelease(Release r, Master m) {
		assertEquals(m, r.getMaster());
		assertEquals("Depeche Mode", r.getDisplayArtist());
		assertEquals("US", r.getCountry());
		assertEquals("1990-03-20", r.getReleased());
		assertEquals("Correct", r.getDataQuality());
		assertEquals("Accepted", r.getStatus());
		assertFalse(r.isMain());
		assertEquals("World In My Eyes", r.getTracklist().getFirst().getTitle());
		assertTrue(r.getGenres().stream().allMatch(x -> "Electronic".equals(x.getName())));
		assertTrue(r.getStyles().stream().allMatch(x -> "Synth-pop".equals(x.getName())));
		assertTrue(r.getExtraArtists().stream().filter(x -> x.getArtist() != null && "Alan Gregorie".equals(x.getArtist().getName())).findFirst().isPresent());
		
		List<Track> tracklist = r.getTracklist();
		
		assertEquals(9, tracklist.size());
		
		Track t = tracklist.get(5);
		
		assertNotNull(t);
		assertEquals("6", t.getPosition());
		assertEquals(6, t.getTrackNumber());
		assertEquals("6:12", t.getDuration());
		assertTrue(t.getArtists().isEmpty());
		
		List<ExtraArtist> eas = t.getExtraArtists();
		
		Optional<ExtraArtist> any = eas.stream().filter(x -> x.getArtist() != null && "Flood".equals(x.getArtist().getName())).findAny();
		
		assertTrue(any.isPresent());
		assertEquals("Mixed By", any.get().getRole());
		assertEquals("Flood", any.get().getArtist().getName());
	}
}
