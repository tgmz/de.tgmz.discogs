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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.CompanyRole;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.DBDefrag;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.discogs.setup.DiscogsFile;
import de.tgmz.mp3.discogs.load.predicate.DataQualityFilter;
import de.tgmz.mp3.discogs.load.predicate.IgnoreUpToFilter;
import de.tgmz.mp3.discogs.load.predicate.MainFilter;
import jakarta.persistence.EntityManager;

public class DiscogsTest {
	private static EntityManager em;
	private static final long IGNORED = 115L;
	
	@BeforeClass
	public static void setupOnce() throws IOException {
		System.setProperty("jakarta.persistence.jdbc.url", "jdbc:h2:mem:discogs_test_mem");
		System.setProperty("jakarta.persistence.jdbc.user", "sa");
		System.setProperty("jakarta.persistence.jdbc.password", "sa");
		System.setProperty(DiscogsFile.DISCOGS_DIR, System.getProperty("java.io.tmpdir"));
		
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
		
		load();
	}
	
	@AfterClass
	public static void teardownOnce() {
		em.close();
		
		new DBDefrag().run();
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
		Artist yashaConen = em.find(Artist.class, 910685L);
		assertEquals("Yasha Conen", yashaConen.getName());
		assertEquals("Moabeat", yashaConen.getGroups().stream().filter(a -> a.getId() == 202_465L).findAny().orElseThrow().getName());
		
		String displayArtist = "Marteria • Yasha • Miss Platnum";
		
		assertEquals(displayArtist, em.find(Master.class, 482870L).getDisplayArtist());
		
		Release lilaWolken = em.find(Release.class, 3870362L);
		
		assertEquals(displayArtist, lilaWolken.getDisplayArtist());
		
		CompanyRole cr = lilaWolken.getCompanies().stream().filter(k -> k.getCompany().getId() == 264516 && "Copyright (c)".equals(k.getRole())).findAny().orElseThrow();
		assertEquals("Four Music Productions GmbH", cr.getCompany().getName());
	}
	@Test
	public void testDecca() {
		Release r = em.find(Release.class, 10850325L);
		
		assertEquals("Germany", r.getCountry());
		
		Track t = r.getTracklist().get(3);
		
		assertEquals("Die Zauberflöte", t.getTitle());
		assertEquals(3, t.sizeOf());
		
		SubTrack st = t.getSubTracklist().getFirst();
		
		assertEquals("Ouvertüre", st.getTitle());
		assertEquals(2, st.sizeOf());
		
		ExtraArtist ea = st.getExtraArtists().stream().filter(ea0 -> ea0.getArtist().getId() == 754974).findFirst().orElseThrow();
		
		assertEquals("Wiener Philharmoniker", ea.getArtist().getName());
		assertEquals("Orchestra", ea.getRole());
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
	public void testExtraArtist() {
		Artist a0 = new Artist(1);
		a0.setName("A");
		
		Artist a1 = new Artist(1);
		a1.setName("B");
		
		
		ExtraArtist ea0 = new ExtraArtist(a0, "Mixed By");
		ExtraArtist ea1 = new ExtraArtist(a1, "Mixed By");

		// Ensure that ExtraArtists are equal iff the artists _ids_ and roles are equal
		assertEquals(ea0, ea1);
	}
	@Test
	public void testGenreStyle() {
		assertTrue(em.createQuery("FROM Genre", Genre.class).getResultStream().anyMatch(x -> "Rock".equals(x.getId())));
		
		assertTrue(em.createQuery("FROM Style", Style.class).getResultStream().anyMatch(x -> "Synth-pop".equals(x.getId())));
	}
	private static void load() throws IOException {
		try (InputStream is = new FileInputStream(DiscogsFile.ARTISTS.getUnzippedFile())) {
			new ArtistContentHandler().run(is);
		}
		
		try (InputStream is = new FileInputStream(DiscogsFile.LABELS.getUnzippedFile())) {
			new LabelContentHandler().run(is);
		}
		
		try (InputStream is = new FileInputStream(DiscogsFile.MASTERS.getUnzippedFile())) {
			new MasterContentHandler(x -> x.getId() != IGNORED).run(is);
		}
		
		Predicate<Release> p0 = new IgnoreUpToFilter();
		Predicate<Release> p1 = new MainFilter();
		Predicate<Release> p2 = new DataQualityFilter(DataQuality.values());
		Predicate<Release> p3 = new IgnoreUpToFilter(1);
		
		Predicate<Release> p = p0.or(p1).or(p2).or(p3);
		
		try (InputStream is = new FileInputStream(DiscogsFile.RELEASES.getUnzippedFile())) {
			new ReleaseContentHandler(p).run(is);
		}
	}
	private void checkArtist(Artist a) {
		assertEquals("Depeche Mode", a.getName());
		assertTrue(a.getVariations().contains("D M"));
		assertEquals(DataQuality.NEEDS_VOTE, a.getDataQuality());
		
		Artist alanWilder = a.getMembers().stream().filter(a0 -> a0.getId() == 25411).findFirst().orElseThrow();
		
		assertEquals("Alan Wilder", alanWilder.getName());
		
		assertTrue(alanWilder.getVariations().contains("A. Wilder"));

		Artist blackSwarm = a.getAliases().stream().filter(a0 -> a0.getId() == 3258245).findFirst().orElseThrow();
		
		assertEquals("Black Swarm", blackSwarm.getName());
	}
	private void checkLabel(Label l) {
		assertEquals("Mute", l.getName());
		assertEquals(DataQuality.NEEDS_VOTE, l.getDataQuality());
		
		assertEquals("Mute Artists Ltd.",  l.getParentLabel().getName());
	}
	private void checkMaster(Master m) {
		assertEquals("Violator", m.getTitle());
		assertEquals(DataQuality.CORRECT, m.getDataQuality());
		assertEquals(1990, m.getPublished().intValue());
		assertTrue(m.getGenres().stream().anyMatch(x -> x.getId().equals("Electronic")));
		assertTrue(m.getStyles().stream().anyMatch(x -> x.getId().equals("Synth-pop")));
	}
	private void checkRelease(Release r, Master m, Label l) {
		assertEquals(m, r.getMaster());
		assertEquals("Depeche Mode", r.getDisplayArtist());
		assertEquals("US", r.getCountry());
		assertEquals("1990-03-20", r.getReleased());
		assertEquals(DataQuality.CORRECT, r.getDataQuality());
		assertFalse(r.isMain());
		assertEquals("World In My Eyes", r.getUnfilteredTracklist().getFirst().getTitle());
		assertTrue(r.getGenres().stream().anyMatch(x -> "Electronic".equals(x.getId())));
		assertTrue(r.getStyles().stream().anyMatch(x -> "Synth-pop".equals(x.getId())));
		
		assertEquals(199, r.sizeOf());
		
		ExtraArtist af = r.getExtraArtists().stream().filter(ea -> 132774 == ea.getArtist().getId()).findAny().orElseThrow();

		assertEquals("Andrew Fletcher", af.getArtist().getName());
		assertEquals("Performer", af.getRole());
		
		List<Track> applicableTracks = r.getTracklist().stream().filter(t -> t.isApplicable("1 to 5, 7 to 9")).toList();

		Artist fk = em.find(Artist.class, 20662); // François Kevorkian
		ExtraArtist efk = new ExtraArtist(fk, "Mixed By");
		
		assertTrue(applicableTracks.stream().allMatch(t -> t.getExtraArtists().contains(efk)));
		
		assertFalse(r.getTracklist().get(5).getExtraArtists().contains(efk));

		Optional<ExtraArtist> ofk = r.getTracklist().get(5).getExtraArtists().stream().filter(ea -> 20662 == ea.getArtist().getId()).findAny();

		assertFalse(ofk.isPresent());
		
		List<Track> tracklist = r.getUnfilteredTracklist();
		
		assertEquals(9, tracklist.size());
		
		Track t = tracklist.get(5);
		
		assertNotNull(t);
		assertEquals("6", t.getPosition());
		assertEquals(6, t.getTrackNumber());
		assertEquals("6:12", t.getDuration());
		assertTrue(t.getArtists().isEmpty());
		
		Set<ExtraArtist> eas = t.getExtraArtists();
		
		ExtraArtist flood = eas.stream().filter(x -> x.getArtist() != null && 20661 == x.getArtist().getId()).findAny().orElseThrow();
		
		assertEquals("Mixed By", flood.getRole());
		assertEquals("Flood", flood.getArtist().getName());
		assertEquals("Mark Ellis", flood.getArtist().getRealName());
		
		assertEquals("9 26081-2", r.getLabels().get(l));
	}
}
