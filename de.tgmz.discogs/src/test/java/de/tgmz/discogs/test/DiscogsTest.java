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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jline.utils.Log;
import org.junit.Test;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Company;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Format;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.ReleaseCompany;
import de.tgmz.discogs.domain.ReleaseExtraArtist;
import de.tgmz.discogs.domain.Series;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import de.tgmz.discogs.domain.id.ReleaseCompanyKey;
import de.tgmz.discogs.domain.id.ReleaseExtraArtistKey;
import de.tgmz.discogs.domain.id.SubTrackId;
import de.tgmz.discogs.load.DiscogsContentHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public abstract class DiscogsTest {
	protected static Path dataDir;
	
	protected static EntityManager em;
	
	protected static void setupOnce() throws IOException {
		dataDir = Files.createTempDirectory("discogs_test");

		String jdbcProtocol = "jdbc:h2:mem:discogs";
		String jdbcProperties = ";MODE=DB2;DEFAULT_NULL_ORDERING=HIGH";
		
		String jdbcUrl = jdbcProtocol + jdbcProperties;

		System.setProperty("jakarta.persistence.jdbc.url", jdbcUrl);
		System.setProperty("jakarta.persistence.jdbc.user", "sa");
		System.setProperty("jakarta.persistence.jdbc.password", "sa");
		System.setProperty("DISCOGS_TEST", "true");
		
		em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager();
	}
	
	protected static void teardownOnce() throws IOException {
		em.close();
		
		try (Stream<Path> walk = Files.walk(dataDir)) {
		    walk.sorted(Comparator.reverseOrder())
		        .map(Path::toFile)
		        .forEach(File::delete);
		}
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
		
		assertEquals(4, lilaWolken.getReleaseCompanies().size());
		
		// Four Music Productions GmbH
		List<ReleaseCompany> fmp = lilaWolken.getReleaseCompanies().stream().filter(rc -> rc.getCompany().getId() == 264516L).toList();
		
		assertEquals(2, fmp.size());
		
		assertTrue(fmp.stream().allMatch(rc -> "Four Music Productions GmbH".equals(rc.getCompany().getName()) 
				&& lilaWolken.equals(rc.getRelease())));

		// Four Music Productions GmbH Copyright
		ReleaseCompany fmpc = fmp.stream().filter(rc -> rc.getEntityType().getId() == 14).findAny().orElseThrow();
		
		assertEquals("Copyright (c)", fmpc.getEntityType().getName());
		
		// Four Music Productions GmbH Phonographic Copyright
		ReleaseCompany fmpp = fmp.stream().filter(rc -> rc.getEntityType().getId() == 13).findAny().orElseThrow();
		
		assertEquals(lilaWolken, fmpp.getRelease());
		assertEquals("Phonographic Copyright (p)", fmpp.getEntityType().getName());
		
		assertTrue(fmpp.toString().contains(band));
	}
	@Test
	public void testBeautifulMaladies() {
		Release r = em.find(Release.class, 9293064L);
		
		assertEquals("Beautiful Maladies (The Island Years)", r.getTitle());
		assertEquals("Tom Waits", r.getAlbumArtist());
		
		// Mixed By Biff Dawes
		ReleaseExtraArtist mbbd = getExtraArtist(r, 281036, "Mixed By");
		
		assertEquals(Set.of("22", "4", "17", "21", "1", "2", "8", "9", "14", "5", "12", "10", "13", "19"), mbbd.getApplicableTracks());
	}
	@Test
	public void test3DoorsDown() {
		Release r = em.find(Release.class, 34334509L);
		
		Track t = r.getTracklist().getFirst();
		
		assertEquals(4, t.getExtraArtists().size());
	}
	@Test
	public void testApplicableTracks() {
		Release r = em.find(Release.class, 2324L);
		
		// Mixed by GusGus
		ReleaseExtraArtist mbgg = em.find(ReleaseExtraArtist.class, new ReleaseExtraArtistKey(r.getId(), "Mixed By", 231513));
		
		// "1 to 4, 6 to 11"
		Set<String> applicableTracks = mbgg.getApplicableTracks();
		
		assertTrue("ExtraArtist applies to tracks 6 to 11 but isApplicable returned false for track 8", r.getTracklist().get(7).isApplicable(applicableTracks));
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
		
		Set<Format> formats = r.getFormats();
		
		assertEquals(1, formats.size());
		
		Format format = formats.stream().findFirst().orElseThrow();
		
		assertEquals("CD", format.getName());
		assertEquals(1, format.getQty().intValue());
		assertTrue(format.getDescriptions().contains("Compilation"));
		assertTrue(format.getDescriptions().contains("Remastered"));
	}
	@Test
	public void testPurpleRain() {
		Release r = em.find(Release.class, 33418256L);
		
		assertEquals("Prince And The Revolution", r.getAlbumArtist());
		assertEquals("Purple Rain", r.getTitle());

		// Assert that subRole is not splitted by ","
		ReleaseExtraArtist prb = getExtraArtist(r, 28795, "Remastered By [LP Remastered, 2015]");
		
		assertEquals("Prince", prb.getExtraArtist().getArtist().getName());
	}
	@Test
	public void testSubtrack() {
		Release r = em.find(Release.class, 2460568L);
		assertTrue(r.getUnfilteredTracklist().get(10).getSubTracklist().isEmpty());
		assertEquals("Sometimes I Feel Like A Motherless Child", r.getTracklist().get(11).getSubTracklist().get(0).getTitle());
		assertEquals("1-12a", r.getTracklist().get(11).getSubTracklist().get(0).getPosition());
		assertEquals(12, r.getUnfilteredTracklist().get(11).getTrackNumber());
		assertEquals(12, r.getTracklist().get(11).getTrackNumber());
		
		Track t0 = new Track(r);
		
		SubTrackId sti0 = new SubTrackId();
		SubTrackId sti1 = new SubTrackId();
		
		sti0.setTrack(t0);
		sti1.setTrack(t0);
		
		sti0.setSubTrackNumber((short) 1);
		sti1.setSubTrackNumber((short) 1);
		
		assertEquals(sti0, sti1);
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
		
		String mixedBy = "Mixed By";
		
		ExtraArtist ea0 = new ExtraArtist(mixedBy, a0);
		ExtraArtist ea1 = new ExtraArtist(mixedBy, a1);

		// Ensure that ExtraArtists are equal iff the artists _ids_ and roles are equal
		assertEquals(ea0, ea1);
	}
	@Test
	public void testGenreStyle() {
		assertTrue(em.createQuery("FROM Genre", Genre.class).getResultStream().anyMatch(x -> "Rock".equals(x.getId())));
		
		assertTrue(em.createQuery("FROM Style", Style.class).getResultStream().anyMatch(x -> "Synth-pop".equals(x.getId())));
	}

	@Test
	public void testEqualsHashcode() {
		for (Class<?> clz : List.of(Genre.class, Style.class, Company.class
				, EntityType.class, Series.class, ReleaseCompanyKey.class, ReleaseExtraArtistKey.class)) {
			EqualsVerifier.forClass(clz)
			.suppress(Warning.SURROGATE_KEY)
			.verify();
		}
	}
	
	@Test
	public void testArtistNoId() {
		assertNull(em.find(Artist.class, 0L));
	}
	@Test
	public void testSeries() {
		Release r = em.find(Release.class, 20279608L);
		
		Series s = r.getSeries();
		
		assertEquals(117965, (int) s.getId());
		assertEquals("World Network", s.getName());
		assertEquals("16", s.getCatno());
	}

	@Test
	public void testFormat() {
		Release r0 = em.find(Release.class, 22838444);
		
		assertEquals("Defaultest 93", r0.getTitle());
		
		Format f0 = r0.getFormats().stream().findFirst().orElseThrow();
		
		assertEquals("File", f0.getName());
		assertEquals("Anti-release", f0.getText());
		assertEquals(Float.POSITIVE_INFINITY, f0.getQty().floatValue(), 0f);
		
		assertEquals("FLAC", f0.getDescriptions().stream().findFirst().orElseThrow());
		
		Release r1 = em.find(Release.class, 4957101);
		
		assertEquals(3, r1.getFormats().size());
		
		Format f1 = r1.getFormats().stream().filter(f -> "CD".equals(f.getName())).findFirst().orElseThrow();
		assertEquals(41, f1.getQty().floatValue(), 0);
		
		Release r2 = em.find(Release.class, 2723);
		
		assertEquals("Out There And Back", r2.getTitle());
		assertEquals(2, r2.getFormats().size());
		assertTrue(r2.getFormats().stream().allMatch(f -> "CD".equals(f.getName())
														&& "".equals(f.getText())
														&& 1f == f.getQty())); 
		
	}

	protected static void extractAndProcess(String resource, DiscogsContentHandler dch) throws IOException {
		URL aUrl = null;
		
		try (DiscogsFileHandler dfh = new DiscogsFileHandler()) {
			aUrl = DiscogsTest.class.getClassLoader().getResource(resource);
			
			File zipped = new File(aUrl.toURI());
			
			dfh.verify(zipped);
			
			File extracted = dfh.extract(zipped, dataDir);
			
			try (InputStream is = new FileInputStream(extracted)) {
				dch.run(is);
			}
		} catch (URISyntaxException e) {
			Log.error("Invalid URI {}", aUrl);
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

		// Ensure that "Performer, Lead Vocals Dave Gahan" is split into 2 separate ExtraArtists
		ReleaseExtraArtist pdg = getExtraArtist(r, 27158, "Performer");
		ReleaseExtraArtist lvdg = getExtraArtist(r, 27158, "Lead Vocals");
		
		Stream.of(pdg, lvdg).forEach(rea -> assertEquals("Dave Gahan", rea.getExtraArtist().getArtist().getName()));
		
		// 21 ExtraArtists apply to all 9 Tracks: => 21 * 9 == 189
		// (remember that "Performer, Lead Vocals Dave Gahan" is split into 2 ExtraArtists)
		// One ExtraArtist (Mixed By François Kevorkian) applies to tracks 1 to 5, 7 to 9 i.e. it does NOT apply to track 6: => 189 + 8 == 197
		// Track 6 has two ExtraArtist: => 197 + 2 == 199
		// No Track has SubTracks: => 199
		assertEquals(199, r.sizeOf());
		
		// Performer Andrew Fletcher
		ReleaseExtraArtist paf = getExtraArtist(r, 132774, "Performer");

		assertEquals("Andrew Fletcher", paf.getExtraArtist().getArtist().getName());
		assertTrue(paf.getApplicableTracks().isEmpty());
		
		// Mixed By François Kevorkian
		ReleaseExtraArtist mbfk = getExtraArtist(r, 20662, "Mixed By");
		Set<String> tracks = mbfk.getApplicableTracks();
		
		assertEquals(Set.of("1 to 5", "7 to 9"), tracks);
		assertTrue(r.getTracklist().getFirst().isApplicable(tracks));
		assertFalse(r.getTracklist().get(5).isApplicable(tracks));

		List<Track> tracklist = r.getUnfilteredTracklist();
		
		assertEquals(9, tracklist.size());
		
		Track t = tracklist.get(5);
		
		assertNotNull(t);
		assertEquals("6", t.getPosition());
		assertEquals(6, t.getTrackNumber());
		assertEquals("6:12", t.getDuration());
		assertTrue(t.getArtists().isEmpty());
		
		Set<ExtraArtist> eas = t.getExtraArtists();
		
		// Mixed By Flood
		ExtraArtist mbf = eas.stream().filter(x -> x.getArtist() != null && 20661 == x.getArtist().getId()).findAny().orElseThrow();
		
		assertEquals("Mixed By", mbf.getRole());
		assertEquals("Flood", mbf.getArtist().getName());
		assertEquals("Mark Ellis", mbf.getArtist().getRealname());
		
		assertEquals("9 26081-2", r.getLabels().get(l));
	}
	
	@Test
	public void testLabel() {
		Label repriseRecords = em.find(Label.class, 157);
		
		assertEquals("Reprise Records", repriseRecords.getName());
		assertEquals("Reprise Records Inc.", repriseRecords.getParentLabel().getName());
		
		Release tin = em.find(Release.class, 2324);
		assertEquals("This Is Normal", tin.getTitle());
		
		Label fad = em.find(Label.class, 634);
		assertEquals("CAD 9006 CD", tin.getLabels().get(fad));
	}
	
	private ReleaseExtraArtist getExtraArtist(Release r, int artistId, String role) {
		return r.getReleaseExtraArtists()
			.stream()
			.filter(rea -> rea.getExtraArtist().getArtist().getId() == artistId && role.equals(rea.getExtraArtist().getRole()))
			.findFirst()
			.orElseThrow();
	}
}
