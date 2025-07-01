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
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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
import jakarta.persistence.TypedQuery;

public class ReleaseContentHandler extends FilteredContentHandler {
	@SuppressWarnings("serial")
	private class ArtistNotFoundException extends ExecutionException {}
	private LoadingCache<Pair<Long, String>, ExtraArtist> extraArtistCache;
	private List<String> displayArtists;
	private List<String> displayJoins;
	private int sequence;
	private int trackNumber;
	private int subTrackNumber;
	private ExtraArtist rea;
	private String tracks;
	private int artistCount = 0;

	public ReleaseContentHandler() {
		this(x -> true);
	}

	public ReleaseContentHandler(Predicate<Discogs> filter) {
		super(filter);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		extraArtistCache = CacheBuilder.newBuilder().build(new CacheLoader<Pair<Long, String>, ExtraArtist>() {
			TypedQuery<ExtraArtist> qea = em.createNamedQuery("ExtraArtist.byArtistIdAndRole", ExtraArtist.class);
			
			@Override
			public ExtraArtist load(Pair<Long, String> key) throws ExecutionException {
				final ExtraArtist[] ea = new ExtraArtist[] {qea.setParameter(1, key.getLeft()).setParameter(2, key.getRight()).getSingleResultOrNull()};
				
				if (ea[0] == null) {
					Artist a = em.find(Artist.class, key.getLeft());
					
					if (a == null) {
						throw new ArtistNotFoundException();
					}
					
					ea[0] = new ExtraArtist(a, key.getRight());
					
					DatabaseService.getInstance().inTransaction(x -> x.persist(ea[0]));
				}
				
				return ea[0];
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
			rea = new ExtraArtist();
			tracks = "";
		
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
			((Release) discogs).getUnfilteredTracklist().getLast().getExtraArtists().add(new ExtraArtist());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks]":
			subTrackNumber = 1;
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track]":
			SubTrack st = new SubTrack();
			st.setTrackNumber(subTrackNumber++);
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().add(st);
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().add(new ExtraArtist());
			
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
			rea.getArtist().setId(Long.parseLong(getChars()));
				
			break;
		case "[releases, release, extraartists, artist, name]":
			rea.getArtist().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, extraartists, artist, role]":
			rea.setRole(getChars());
			
			break;
		case "[releases, release, extraartists, artist, tracks]":
			tracks = getChars();
				
			break;
		case "[releases, release, extraartists, artist]":
			((Release) discogs).getExtraArtists().put(rea, tracks);
				
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
		case "[releases, release, tracklist, track, artists, artist, name]":
			((Release) discogs).getUnfilteredTracklist().getLast().getArtists().getLast().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, id]":
			((Release) discogs).getUnfilteredTracklist().getLast().getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));

			break;
		case "[releases, release, tracklist, track, extraartists, artist, name]":
			((Release) discogs).getUnfilteredTracklist().getLast().getExtraArtists().getLast().getArtist().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, tracklist, track, extraartists, artist, role]":
			((Release) discogs).getUnfilteredTracklist().getLast().getExtraArtists().getLast().setRole(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, position]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().setPosition(getChars());
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, title]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().setTitle(getChars(MAX_LENGTH_DEFAULT));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, id]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().getLast().getArtist().setId(Long.parseLong(getChars()));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, name]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().getLast().getArtist().setName(getChars(MAX_LENGTH_DEFAULT, true));
			
			break;
		case "[releases, release, tracklist, track, sub_tracks, track, extraartists, artist, role]":
			((Release) discogs).getUnfilteredTracklist().getLast().getSubTracklist().getLast().getExtraArtists().getLast().setRole(getChars());
			
			break;
		case "[releases, release]":
			save(discogs);
			
			break;
		default:
		}
		
		super.endElement(uri, localName, qName);
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("{} artists added  ", String.format("%,d", artistCount));
		}
	}
	
	@Override
	protected void fillAttributes(Discogs d) {
		Release r = (Release) d;
		
		r.setTitle(StringUtils.left(discogs.getTitle(), MAX_LENGTH_DEFAULT));

		fillArtists(r.getArtists());

		Map<ExtraArtist, String> copyOf = HashMap.newHashMap(r.getExtraArtists().size());
		
		for (Entry<ExtraArtist, String> eea : r.getExtraArtists().entrySet()) {
			ExtraArtist ea = fillExtraArtist(eea.getKey());
				
			if (ea != null) {
				copyOf.put(ea, eea.getValue());
			}
		}
		
		r.setExtraArtists(copyOf);
		
		for (Track t : r.getUnfilteredTracklist()) {
			fillArtists(t.getArtists());
			fillExtraArtists(t.getExtraArtists());
			
			for (SubTrack st : t.getSubTracklist()) {
				fillExtraArtists(st.getExtraArtists());
			}
		}
		
		Master m = r.getMaster();
			
		if (m != null) {
			r.setMaster(em.find(Master.class, m.getId()));
		}

		r.setLabels(getLabels(r.getLabels()));
	}
	
	private void fillArtists(List<Artist> artists) {
		if (artists != null) {
			artists.replaceAll(a -> a = fillArtist(a));
		}
	}
	
	private void fillExtraArtists(List<ExtraArtist> extraArtists) {
		if (extraArtists != null) {
			extraArtists.replaceAll(ea -> ea = fillExtraArtist(ea));
			extraArtists.removeIf(Objects::isNull);
		}
	}
	
	private Map<Label, String> getLabels(Map<Label, String> labels) {
		Map<Label, String> result = HashMap.newHashMap(labels.size());
		
		for (Entry<Label, String> e : labels.entrySet()) {
			Label l = em.find(Label.class, e.getKey().getId());
			
			if (l != null) {
				result.put(l, e.getValue());
			} else {
				LOG.debug("Label with id {} not found", e.getKey().getId());
			}
		}
		
		return result;
	}
	
	private ExtraArtist fillExtraArtist(ExtraArtist ea) {
		if (ea == null || ea.getArtist() == null || ea.getArtist().getId() == 0 || ea.getArtist().getName() == null) {
			return null;
		}
		
		try {
			return extraArtistCache.get(Pair.of(ea.getArtist().getId(), ea.getRole()));
		} catch (ExecutionException e) {
			return null;
		}
	}
	
	private Artist fillArtist(Artist a0) {
		Artist[] a = new Artist[] {em.find(Artist.class, a0.getId())};
		
		if (a[0] == null) {
			a[0] = new Artist();
			a[0].setId(a0.getId());
			a[0].setName(a0.getName());
			
			DatabaseService.getInstance().inTransaction(x -> x.persist(a[0]));
			
			++artistCount;
		}

		return a[0];
	}
}
