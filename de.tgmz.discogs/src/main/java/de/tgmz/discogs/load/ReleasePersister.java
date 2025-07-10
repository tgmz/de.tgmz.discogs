/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Style;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ReleasePersister {
	private static final Logger LOG = LoggerFactory.getLogger(ReleasePersister.class);
	private Release r;
	private EntityManager em;
	private LoadingCache<String, Genre> genreFactory;
	private LoadingCache<String, Style> styleFactory;
	
	public ReleasePersister() {
		genreFactory = Caffeine.newBuilder().build(new CacheLoader<String, Genre>() {
			@Override
			public Genre load(String key) {
				return new Genre(key);
			}
		});

		styleFactory = Caffeine.newBuilder().build(new CacheLoader<String, Style>() {
			@Override
			public Style load(String key) {
				return new Style(key);
			}
		});
	}

	public void setRelease(Release r) {
		this.r = r;
	}
	
	public void save(EntityManager em0) {
		this.em = em0;
			
		// Master
		if (r.getMaster() != null) {
			r.setMaster(em.find(Master.class, r.getMaster().getId()));
		}
			
		Map<Label, String> result = HashMap.newHashMap(r.getLabels().size());
			
		for (Entry<Label, String> e : r.getLabels().entrySet()) {
			Label l = em.find(Label.class, e.getKey().getId());
			
			if (l != null) {
				result.put(l, e.getValue());
			}
		}
			
		r.setLabels(result);
		
		Set<Genre> gs = HashSet.newHashSet(r.getGenres().size());
		r.getGenres().stream().forEach(g -> gs.add(genreFactory.get(g.getId())));
		r.setGenres(gs);
		
		Set<Style> ss = HashSet.newHashSet(r.getStyles().size());
		r.getStyles().stream().forEach(s -> ss.add(styleFactory.get(s.getId())));
		r.setStyles(ss);
				
		r.getArtists().replaceAll(a -> a = fillArtist(a));
		r.getArtists().removeIf(Objects::isNull);

		Map<ExtraArtist, String> copyOf = HashMap.newHashMap(r.getExtraArtists().size());
			
		for (Entry<ExtraArtist, String> eea : r.getExtraArtists().entrySet()) {
			ExtraArtist ea = fillExtraArtist(eea.getKey());
				
			if (ea != null) {
				copyOf.put(ea, eea.getValue());
			}
		}
			
		r.setExtraArtists(copyOf);
			
		for (Track t : r.getUnfilteredTracklist()) {
			t.getArtists().replaceAll(a -> a = em.find(Artist.class, a.getId()));
			t.getArtists().removeIf(Objects::isNull);
			
			t.getExtraArtists().replaceAll(ea -> ea = fillExtraArtist(ea));
			t.getExtraArtists().removeIf(Objects::isNull);
				
			for (SubTrack st : t.getSubTracklist()) {
				st.getExtraArtists().replaceAll(ea -> ea = fillExtraArtist(ea));
				st.getExtraArtists().removeIf(Objects::isNull);
			}
		}
			
		LOG.debug("Save {}", r);

		em.merge(r);
		
		em.flush();
	}
	
	private ExtraArtist fillExtraArtist(ExtraArtist ea0) {
		if (ea0 == null || ea0.getArtist() == null || ea0.getArtist().getId() == 0 || ea0.getArtist().getName() == null) {
			return null;
		}
		
		TypedQuery<ExtraArtist> qea = em.createNamedQuery("ExtraArtist.byArtistIdAndRole", ExtraArtist.class);
		
		ExtraArtist ea = qea.setParameter(1, ea0.getArtist().getId()).setParameter(2, ea0.getRole()).getSingleResultOrNull();
		
		if (ea == null) {
			LOG.trace("ExtraArtist {} not found", ea0);
			
			Artist a = fillArtist(ea0.getArtist());
			
			ea = new ExtraArtist(a, ea0.getRole());
			
			em.persist(ea);
			
			LOG.trace("New ExtraArtist {} saved", ea);
		}
		
		return ea;
	}
	
	private Artist fillArtist(Artist a0) {
		if (a0 == null || a0.getId() == 0 || a0.getName() == null) {
			return null;
		}
		
		Artist a = em.find(Artist.class, a0.getId());
			
		if (a == null) {
			LOG.trace("Artist {} not found", a0.getId());
				
			a = new Artist();
			a.setId(a0.getId());
			a.setName(a0.getName());
				
			em.persist(a);
				
			LOG.trace("New Artist {} saved", a);
		}
		
		return a;
	}
}
