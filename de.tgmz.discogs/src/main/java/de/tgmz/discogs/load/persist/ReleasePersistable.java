/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Company;
import de.tgmz.discogs.domain.Discogs;
import de.tgmz.discogs.domain.ExtraArtist;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.SubTrack;
import de.tgmz.discogs.domain.Track;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ReleasePersistable implements IPersistable<Release> {
	private static final Logger LOG = LoggerFactory.getLogger(ReleasePersistable.class);
	private Predicate<Discogs> filter;
	private Map<Long, Company> companyCache;
	private EntityManager em;
	
	public ReleasePersistable() {
		this(x -> true);
	}
	
	public ReleasePersistable(Predicate<Discogs> filter) {
		this.filter = filter;
	}
	
	public int save(EntityManager em0, Release r) {
		if (!filter.test(r)) {
			return 0;
		}
		
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
		
		companyCache = new TreeMap<>();
		
		r.getCompanies().forEach(cr -> cr.setCompany(getOrCreate(cr.getCompany())));
			
		em.merge(r);
		
		companyCache.clear();
		
		return 1;
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
				
			a = new Artist(a0.getId());
			a.setName(a0.getName());
				
			em.persist(a);
				
			LOG.trace("New Artist {} saved", a);
		}
		
		return a;
	}
	
	private Company getOrCreate(Company draft) {
		return companyCache.computeIfAbsent(draft.getId(), l -> findOrCreate(em, draft));
	}
	
	private Company findOrCreate(EntityManager em, Company draft) {
		Company c0 = em.find(Company.class, draft.getId());
		
		if (c0 == null) {
			c0 = draft;
		}
		
		return c0;
		
	}
}
