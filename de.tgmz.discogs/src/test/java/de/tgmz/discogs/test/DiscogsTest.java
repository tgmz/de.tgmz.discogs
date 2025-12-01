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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Company;
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
import de.tgmz.discogs.domain.id.SubTrackId;
import de.tgmz.discogs.domain.id.TrackId;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import de.tgmz.discogs.relevance.RelevanceService;
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
		System.setProperty("jakarta.persistence.jdbc.url", AllTests.JDBC_URL);
		System.setProperty("jakarta.persistence.jdbc.user", "sa");
		System.setProperty("jakarta.persistence.jdbc.password", "sa");
		System.setProperty(DiscogsFile.DISCOGS_DIR, System.getProperty("java.io.tmpdir"));
		
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
		
		load();
	}
	
	@AfterClass
	public static void teardownOnce() {
		em.close();
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
		
		String band = "Marteria • Yasha • Miss Platnum";
		
		assertEquals(band, em.find(Master.class, 482870L).getAlbumArtist());
		
		Release lilaWolken = em.find(Release.class, 3870362L);
		
		assertEquals(band, lilaWolken.getAlbumArtist());
		
		CompanyRole cr = lilaWolken.getCompanies().stream().filter(k -> k.getId().getCompany().getId() == 264516 && "Copyright (c)".equals(k.getId().getRole())).findAny().orElseThrow();
		assertEquals("Four Music Productions GmbH", cr.getId().getCompany().getName());
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
		assertEquals("6:29", st.getDuration());
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
	public void testExtraArtistEquals() {
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
	public void testCompanyRoleEquals() {
		Company c0 = new Company(1, "0");
		Company c1 = new Company(1, "1");
		
		CompanyRole cr0 = new CompanyRole(c0, "Copyright");
		CompanyRole cr1 = new CompanyRole(c1, "Copyright");

		// Ensure that CompanyRoles are equal iff the companys _ids_ and roles are equal
		assertEquals(cr0, cr1);
	}
	@Test
	public void testTrackEquals() {
		Release r0 = new Release();
		Release r1 = new Release();
		
		r0.setId(1);
		r1.setId(1);
		
		TrackId ti0 = new TrackId();
		TrackId ti1 = new TrackId();
		
		ti0.setRelease(r0);
		ti1.setRelease(r1);
		
		ti0.setSequence((short) 2);
		ti1.setSequence((short) 2);
		
		// Ensure that trackIds are equal iff the releases _ids_ and sequences are equal
		assertEquals(ti0, ti1);

		Track t0 = new Track(r0);
		Track t1 = new Track(r1);
		
		SubTrackId sti0 = new SubTrackId();
		SubTrackId sti1 = new SubTrackId();
		
		sti0.setTrack(t0);
		sti1.setTrack(t1);

		sti0.setSubTrackNumber((short) 3);
		sti1.setSubTrackNumber((short) 3);
		
		// Ensure that subTracks are equal iff the tracks _ids_ and subtracknumbers are equal
		assertEquals(sti0, sti1);
	}
	@Test
	public void testGenreStyle() {
		assertTrue(em.createQuery("FROM Genre", Genre.class).getResultStream().anyMatch(x -> "Rock".equals(x.getId())));
		
		assertTrue(em.createQuery("FROM Style", Style.class).getResultStream().anyMatch(x -> "Synth-pop".equals(x.getId())));
	}
	@Test
	public void testArtistNoId() {
		assertNull(em.find(Artist.class, 0L));
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
		assertEquals("Depeche Mode", r.getAlbumArtist());
		assertEquals("US", r.getCountry());
		assertEquals("1990-03-20", r.getReleased());
		assertEquals(DataQuality.CORRECT, r.getDataQuality());
		assertFalse(r.isMain());
		assertEquals("World In My Eyes", r.getUnfilteredTracklist().getFirst().getTitle());
		assertTrue(r.getGenres().stream().anyMatch(x -> "Electronic".equals(x.getId())));
		assertTrue(r.getStyles().stream().anyMatch(x -> "Synth-pop".equals(x.getId())));
		
		// 20 ExtraArtists apply to all 9 Tracks: => 180
		// One ExtraArtist (Mixed By François Kevorkian) applies to tracks 1 to 5, 7 to 9 i.e. it does NOT apply to track 6: => 188
		// Track 6 has two ExtraArtist: => 190
		// No Track has SubTracks: => 190
		assertEquals(190, r.sizeOf());
		
		// Performer Andrew Fletcher
		Entry<ExtraArtist, String> paf = getExtraArtist(r, 132774, "Performer");

		assertEquals("Andrew Fletcher", paf.getKey().getArtist().getName());
		assertEquals("Performer", paf.getKey().getRole());
		
		// Mixed By François Kevorkian
		Entry<ExtraArtist, String> embfk = getExtraArtist(r, 20662, "Mixed By");
		ExtraArtist mbfk = embfk.getKey(); 
		String tracks = embfk.getValue();
		
		assertEquals("1 to 5, 7 to 9", tracks);
		assertTrue(r.getTracklist().getFirst().isApplicable(mbfk, tracks));
		assertFalse(r.getTracklist().get(5).isApplicable(mbfk, tracks));

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
		
		RelevanceService.getInstance().setRelevantRoles("Performer", "Producer");
		assertEquals(54, r.sizeOf());
		RelevanceService.getInstance().setRelevantRoles((String[]) null);
	}
	
	private Entry<ExtraArtist, String> getExtraArtist(Release r, long id, String role) {
		return r.getExtraArtists()
				.entrySet()
				.stream()
				.filter(e -> id == e.getKey().getArtist().getId() && role.equals(e.getKey().getRole())).findAny().orElseThrow();
	}
}
