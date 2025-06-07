/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ReleaseContentHandler extends FilteredContentHandler {
	private class TempExtraArtist {
		String role; 
		long artistId;
	}
	private LoadingCache<Long, Artist> artistsCache;
	private LoadingCache<Long, Label> labelCache;
	private LoadingCache<Long, Master> masterCache;
	private LoadingCache<Pair<String, Long>, ExtraArtist> extraArtistCache;
	private TempExtraArtist tea;
	private List<String> displayArtists;
	private List<String> displayJoins;
	private int sequence;
	private int trackNumber;
	private int subTrackNumber;

	public ReleaseContentHandler() {
		this(x -> true);
	}

	public ReleaseContentHandler(Predicate<Discogs> filter) {
		super(filter);
		
		artistsCache = Caffeine.newBuilder().build(new CacheLoader<Long, Artist>() {
			@Override
			public Artist load(Long key) {
				return em.find(Artist.class, key);
			}
		});
		
		labelCache = Caffeine.newBuilder().build(new CacheLoader<Long, Label>() {
			@Override
			public Label load(Long key) {
				return em.find(Label.class, key);
			}
		});
		
		masterCache = Caffeine.newBuilder().build(new CacheLoader<Long, Master>() {
			@Override
			public Master load(Long key) {
				return em.find(Master.class, key);
			}
		});
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		TypedQuery<ExtraArtist> q0 = em.createNamedQuery("ExtraArtist.findByRoleAndArtist", ExtraArtist.class);
		
		extraArtistCache = Caffeine.newBuilder().build(new CacheLoader<Pair<String, Long>, ExtraArtist>() {
			@Override
			public ExtraArtist load(Pair<String, Long> key) {
				Artist a = artistsCache.get(key.getRight());
				
				if (a == null) {
					return null;
				}
				
				try {
					return q0.setParameter(1, key.getLeft()).setParameter(2, a).getSingleResult();
				} catch (NoResultException e) {
					ExtraArtist ea = new ExtraArtist(key.getLeft(), a);

					DatabaseService.getInstance().inTransaction(x -> x.persist(ea));
					
					return ea;
				}
			}
		}); 
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		switch (path) {
		case "[releases, release]":
			discogs = new Release();
			
			discogs.setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case "[releases, release, master_id]":
			((Release) discogs).setMain(Boolean.valueOf(attributes.getValue("is_main_release")));

			break;
		case "[releases, release, artists]":
			displayArtists = new ArrayList<>();
			displayJoins = new ArrayList<>();
			
			break;
		case "[releases, release, artists, artist]":
			discogs.getArtists().add(new Artist());
			
			break;
		case "[releases, release, extraartists, artist]":
			tea = new TempExtraArtist();
		
			break;
		case "[releases, release, tracklist]":
			sequence = 0;
			trackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track]":
			Track t = new Track();
			t.setSequence(sequence++);
			t.setTrackNumber(trackNumber++);
			((Release) discogs).getUnfilteredTracklist().add(t);
			
			break;
		case "[releases, release, tracklist, track, artists]":
			((Release) discogs).getUnfilteredTracklist().getLast().setArtists(new LinkedList<>());
			
			break;
		case "[releases, release, tracklist, track, artists, artist]":
			((Release) discogs).getUnfilteredTracklist().getLast().getArtists().add(new Artist());
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist]":
			tea = new TempExtraArtist();
			
			break;
		case "[releases, release, tracklist, track, sub_tracks]":
			subTrackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track]":
			SubTrack st = new SubTrack();
			st.setTrackNumber(subTrackNumber++);
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().add(st);
			
			break;
		case "[releases, release, labels, label]":
			String id = attributes.getValue("id");
			
			if (id != null) {
				Label l = new Label(); 
				l.setId(Long.parseLong(attributes.getValue("id")));
			
				String catno = attributes.getValue("catno");
			
				((Release) discogs).getLabels().put(l, catno);
			}
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		switch (path) {
		case "[releases, release, master_id]":
			((Release) discogs).setMaster(em.find(Master.class, Long.parseLong(getChars())));

			break;
		case "[releases, release, title]":
			discogs.setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, released]":
			((Release) discogs).setReleased(getChars());
			
			break;
		case "[releases, release, data_quality]":
			discogs.setDataQuality(DataQuality.byName(getChars()));
			
			break;
		case "[releases, release, country]":
			((Release) discogs).setCountry(getChars());
			
			break;
		// artists
		case "[releases, release, artists]":
			discogs.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			
			break;
		case "[releases, release, artists, artist, id]":
			discogs.getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, artists, artist, name]":
			String s = getChars(MAX_LENGTH_DEFAULT, true);
			
			displayArtists.add(s);
			discogs.getArtists().getLast().setName(s);
			
			break;
		case "[releases, release, artists, artist, anv]":
			displayArtists.set(displayArtists.size() - 1, getChars());
			
			break;
		case "[releases, release, artists, artist, join]":
			displayJoins.add(getChars());
			
			break;
		// extraartists
		case "[releases, release, extraartists, artist, id]":
			tea.artistId = Long.parseLong(getChars());
				
			break;
		case "[releases, release, extraartists, artist, role]":
			tea.role = getChars(MAX_LENGTH_DEFAULT);
				
			Artist a0 = new Artist();
			a0.setId(tea.artistId);
			
			ExtraArtist ea0 = new ExtraArtist(tea.role, a0);
			
			((Release) discogs).getExtraArtists().put(ea0, "");
			
			break;
		case "[releases, release, extraartists, artist, tracks]":
			Artist a1 = new Artist();
			a1.setId(tea.artistId);
			
			ExtraArtist ea1 = new ExtraArtist(tea.role, a1);
			
			((Release) discogs).getExtraArtists().put(ea1, getChars());
				
			break;
		//tracks
		case "[releases, release, tracklist, track]":
			Track t = ((Release) discogs).getUnfilteredTracklist().getLast();
			
			if (t.getPosition() == null && t.getSubTracklist().isEmpty()) {
				// Not al "real" track (e.g. headline like "Roots" and "The Roots of Sepultura") 
				--trackNumber;
			}
			
			break;
		case "[releases, release, tracklist, track, position]":
			((Release) discogs).getUnfilteredTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, duration]":
			((Release) discogs).getUnfilteredTracklist().getLast().setDuration(getChars());
			
			break;
		case "[releases, release, tracklist, track, title]":
			((Release) discogs).getUnfilteredTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
				
			break;
		case "[releases, release, tracklist, track, artists, artist, id]":
			((Release) discogs).getUnfilteredTracklist().getLast().getArtists().getLast().setId(Long.valueOf(getChars()));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, id]":
			tea.artistId = Long.parseLong(getChars());

			break;
		case "[releases, release, tracklist, track, extraartists, artist, role]":
			tea.role = getChars(MAX_LENGTH_DEFAULT);

			Artist a2 = new Artist();
			a2.setId(tea.artistId);
			
			ExtraArtist ea2 = new ExtraArtist(tea.role, a2);
			
			((Release) discogs).getUnfilteredTracklist().getLast().getExtraArtists().add(ea2);
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, position]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, title]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release]":
			save(discogs);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	
	@Override
	protected void fillAttributes(Discogs d) {
		Release r = (Release) d;
		
		r.setTitle(StringUtils.left(discogs.getTitle(), MAX_LENGTH_DEFAULT));

		fillArtists(r.getArtists());
		
		Map<ExtraArtist, String> temp = new HashMap<>(r.getExtraArtists());
		r.getExtraArtists().clear();
		
		for (Entry<ExtraArtist, String> e : temp.entrySet()) {
			ExtraArtist ea = extraArtistCache.get(Pair.of(e.getKey().getRole(), e.getKey().getArtist().getId()));
			
			if (ea != null) {
				r.getExtraArtists().put(ea, e.getValue());
			}
		}
			
		for (Track t : r.getUnfilteredTracklist()) {
			fillArtists(t.getArtists());
			fillExtraArtists(t.getExtraArtists());
		}

		Master m = r.getMaster();
			
		if (m != null) {
			r.setMaster(masterCache.get(m.getId()));
		}

		r.setLabels(getLabels(r.getLabels()));
	}
	
	private void fillArtists(List<Artist> artists) {
		if (artists != null) {
			artists.replaceAll(a -> a = artistsCache.get(a.getId()));
			artists.removeIf(a -> a == null);
		}
	}
	
	private void fillExtraArtists(List<ExtraArtist> extraArtists) {
		if (extraArtists != null) {
			extraArtists.replaceAll(ea -> ea = extraArtistCache.get(Pair.of(ea.getRole(), ea.getArtist().getId())));
			extraArtists.removeIf(Objects::isNull);
		}
	}
	
	private Map<Label, String> getLabels(Map<Label, String> labels) {
		Map<Label, String> result = new HashMap<>();
		
		for (Entry<Label, String> e : labels.entrySet()) {
			Label l = labelCache.get(e.getKey().getId());
			
			if (l != null) {
				result.put(l, e.getValue());
			} else {
				LOG.debug("Label with id {} not found", e.getKey().getId());
			}
		}
		
		return result;
	}
}
