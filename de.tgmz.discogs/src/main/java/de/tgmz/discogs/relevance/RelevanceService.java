/*********************************************************************
* Copyright (c) 08.12.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.relevance;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tgmz.discogs.database.DatabaseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

public class RelevanceService {
	private static final RelevanceService INSTANCE = new RelevanceService();
	
	private List<Class<?>> entities;
	
	private RelevanceService() {
		entities = new LinkedList<>();
		
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			Set<EntityType<?>> ets = em.getMetamodel().getEntities();
			
			ets.forEach(e -> entities.add(e.getJavaType()));
		}
	}

	public static RelevanceService getInstance() {
		return INSTANCE;
	}

	public void setRelevantEntities(Class<?>... entities) {
		this.entities.clear();

		this.entities.addAll(Arrays.asList(entities));
	}
	
	public void removeRelevantEntities(Class<?>... entities) {
		this.entities.removeIf(c -> Arrays.asList(entities).contains(c));
	}
	
	public boolean isRelevant(Object o) {
		return isRelevant(o.getClass());
	}

	public boolean isRelevant(Class<?> entity) {
		return entities.contains(entity);
	}
}
