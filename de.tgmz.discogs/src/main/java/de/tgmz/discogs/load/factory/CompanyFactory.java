/*********************************************************************
* Copyright (c) 11.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import java.util.Map;
import java.util.TreeMap;

import de.tgmz.discogs.domain.Company;
import jakarta.persistence.EntityManager;

public class CompanyFactory implements IFactory<Company> {
	private Map<Long, Company> cache;
	
	public CompanyFactory() {
		cache = new TreeMap<>();
	}
	
	@Override
	public Company get(EntityManager em, Company draft) {
		return getOrCreate(em, draft);
	}
	
	private Company getOrCreate(EntityManager em, Company draft) {
		return cache.computeIfAbsent(draft.getId(), l -> findOrCreate(em, draft));
	}
	
	private Company findOrCreate(EntityManager em, Company draft) {
		Company c = em.find(Company.class, draft.getId());
		
		if (c == null) {
			c = draft;
			
			em.persist(c);
		}
		
		return c;
	}
}
