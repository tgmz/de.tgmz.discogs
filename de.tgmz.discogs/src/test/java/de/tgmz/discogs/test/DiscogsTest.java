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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.discogs.logging.LogUtil;
import de.tgmz.discogs.setup.DiscogsFile;
import de.tgmz.mp3.discogs.load.predicate.CacheFilter;
import de.tgmz.mp3.discogs.load.predicate.DataQualityFilter;
import de.tgmz.mp3.discogs.load.predicate.IgnoreReleasesUpToFilter;
import de.tgmz.mp3.discogs.load.predicate.MainFilter;
import jakarta.persistence.EntityManager;

public class DiscogsTest {
	private static EntityManager em;
	private static final long IGNORED = 115L;
	
	@BeforeClass
	public static void setupOnce() throws IOException, SAXException {
		System.setProperty("DB_URL", "jdbc:h2:mem:discogs_test_mem");
		System.setProperty("DB_USR", "sa");
		System.setProperty("DB_PASS", "sa");
		System.setProperty(DiscogsFile.DISCOGS_DIR, System.getProperty("java.io.tmpdir"));
		
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
		
		load();
	}
	
	@AfterClass
	public static void teardownOnce() {
		em.close();
		
		LogUtil.logElapsed();
	}
	
	@Test
	public void testViolator() {
		Artist a = em.find(Artist.class, 2725L);
		checkArtist(a);
		
		Label l = em.find(Label.class, 26391L);
		checkLabel(l);
		
		Master m = em.find(Master.class, 18080L);
		checkMaster(m);

		Release r = em.find(Release.class, 10222L);
		checkRelease(r, m, l);
	}
	@Test
	public void testLilaWolken() {
		assertEquals("Yasha Conen", em.find(Artist.class, 910685L).getName());
		
		String displayArtist = "Marteria • Yasha • Miss Platnum";
		
		assertEquals(displayArtist, em.find(Master.class, 482870L).getDisplayArtist());
		assertEquals(displayArtist, em.find(Release.class, 3870362L).getDisplayArtist());
	}
	@Test
	public void testSubtrack() {
		Release r = em.find(Release.class, 2460568L);
		assertTrue(r.getUnfilteredTracklist().get(10).getSubTracklist().isEmpty());
		assertEquals("Sometimes I Feel Like A Motherless Child", r.getTracklist().get(11).getSubTracklist().get(0).getTitle());
		assertEquals("1-12a", r.getTracklist().get(11).getSubTracklist().get(0).getPosition());
		assertEquals(12, r.getUnfilteredTracklist().get(11).getTrackNumber());
		assertEquals(12, r.getTracklist().get(11).getTrackNumber());
	}
	@Test
	public void testEmptyTrack() {
		Release r = em.find(Release.class, 20279608L);
		
		List<Track> tl = r.getUnfilteredTracklist();
		
		assertEquals(25, tl.size());
		assertEquals(1, tl.get(0).getTrackNumber());
		assertEquals(22, tl.get(24).getTrackNumber());
		
		assertEquals(0, tl.get(0).getSequence());
		assertEquals(24, tl.get(24).getSequence());
		
		List<Track> ftl = r.getTracklist();
		
		assertEquals(22, ftl.size());
		assertEquals(1, ftl.get(0).getTrackNumber());
		assertEquals(22, ftl.get(21).getTrackNumber());
		
		assertEquals(1, ftl.get(0).getSequence());
		assertEquals(24, ftl.get(21).getSequence());
	}
	@Test
	public void testGenreStyle() {
		assertTrue(em.createQuery("FROM Genre", Genre.class).getResultStream().anyMatch(x -> "Rock".equals(x.getId())));
		
		assertTrue(em.createQuery("FROM Style", Style.class).getResultStream().anyMatch(x -> "Synth-pop".equals(x.getId())));
	}
	private static void load() throws IOException,SAXException {
		try (InputStream is = new FileInputStream(DiscogsFile.ARTISTS.getUnzippedFile())) {
			new ArtistContentHandler().run(is);
		}
		
		try (InputStream is = new FileInputStream(DiscogsFile.LABELS.getUnzippedFile())) {
			new LabelContentHandler().run(is);
		}
		
		try (InputStream is = new FileInputStream(DiscogsFile.MASTERS.getUnzippedFile())) {
			new MasterContentHandler(x -> x.getId() != IGNORED).run(is);
		}
		
		Predicate<Discogs> p0 = new MainFilter();
		Predicate<Discogs> p1 = new DataQualityFilter(DataQuality.values());
		Predicate<Discogs> p2 = new IgnoreReleasesUpToFilter();
		
		Predicate<Discogs> p = p0.and(p1).and(p2);
		
		Predicate<Discogs> p3 = new CacheFilter();
		
		p = p.or(p3);
		
		try (InputStream is = new FileInputStream(DiscogsFile.RELEASES.getUnzippedFile())) {
			new ReleaseContentHandler(p).run(is);
		}
	}
	private void checkArtist(Artist a) {
		assertEquals("Depeche Mode", a.getName());
		assertTrue(a.getVariations().contains("D M"));
		assertEquals(DataQuality.NEEDS_VOTE, a.getDataQuality());
		
		Optional<Artist> alanWilder = a.getMembers().stream().filter(a0 -> a0.getId() == 25411).findFirst();
		
		assertTrue(alanWilder.isPresent());
		assertEquals("Alan Wilder", alanWilder.get().getName());
		
		assertTrue(alanWilder.get().getVariations().contains("A. Wilder"));
	}
	private void checkLabel(Label l) {
		assertEquals("Mute", l.getName());
		assertEquals(DataQuality.NEEDS_VOTE, l.getDataQuality());
		
		Optional<Label> ops = l.getSubLabels().stream().filter(l0 -> l0.getId() == 12954).findAny();
		assertTrue(ops.isPresent());
		
		assertEquals("Parallel Series", ops.get().getName());
		assertEquals(DataQuality.NEEDS_VOTE, ops.get().getDataQuality());
	}
	private void checkMaster(Master m) {
		assertEquals("Violator", m.getTitle());
		assertEquals(DataQuality.CORRECT, m.getDataQuality());
		assertEquals(1990, m.getPublished().intValue());
		assertTrue(m.getGenres().stream().allMatch(x -> x.getId().equals("Electronic")));
		assertTrue(m.getStyles().stream().allMatch(x -> x.getId().equals("Synth-pop")));
	}
	private void checkRelease(Release r, Master m, Label l) {
		assertEquals(m, r.getMaster());
		assertEquals("Depeche Mode", r.getDisplayArtist());
		assertEquals("US", r.getCountry());
		assertEquals("1990-03-20", r.getReleased());
		assertEquals(DataQuality.CORRECT, r.getDataQuality());
		assertFalse(r.isMain());
		assertEquals("World In My Eyes", r.getUnfilteredTracklist().getFirst().getTitle());
		assertTrue(r.getGenres().stream().allMatch(x -> "Electronic".equals(x.getId())));
		assertTrue(r.getStyles().stream().allMatch(x -> "Synth-pop".equals(x.getId())));
		
		Optional<ExtraArtist> fk = r.getExtraArtists().stream().filter(x -> x.getArtist() != null && 20662 == x.getArtist().getId()).findAny();
		assertTrue(fk.isPresent());
		assertEquals("François Kevorkian", fk.get().getArtist().getName());
		assertEquals("Mixed By", fk.get().getRole());
		assertEquals(Set.of("1 to 5", "7 to 9"), fk.get().getTracks());
		
		List<Track> tracklist = r.getUnfilteredTracklist();
		
		assertEquals(9, tracklist.size());
		
		Track t = tracklist.get(5);
		
		assertNotNull(t);
		assertEquals("6", t.getPosition());
		assertEquals(6, t.getTrackNumber());
		assertEquals("6:12", t.getDuration());
		assertTrue(t.getArtists().isEmpty());
		
		List<ExtraArtist> eas = t.getExtraArtists();
		
		Optional<ExtraArtist> flood = eas.stream().filter(x -> x.getArtist() != null && 20661 == x.getArtist().getId()).findAny();
		
		assertTrue(flood.isPresent());
		assertEquals("Mixed By", flood.get().getRole());
		assertEquals("Flood", flood.get().getArtist().getName());
		assertEquals(flood.get(), new ExtraArtist("Mixed By", flood.get().getArtist(), Collections.emptySet()));
		
		assertEquals("9 26081-2", r.getLabels().get(l));
	}
}
