/*********************************************************************
* Copyright (c) 03.04.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.mp3.discogs.load.predicate;

import java.util.function.Predicate;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Release;
import jakarta.persistence.EntityManager;

public class IgnoreUpToFilter implements Predicate<Release> {
	private long rMaxId;
	
	public IgnoreUpToFilter() {
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			this.rMaxId = (long) em.createNativeQuery("SELECT COALESCE(MAX(id), 0) FROM Release", Long.class).getSingleResult();
		}
	}
	
	public IgnoreUpToFilter(long maxId) {
		this.rMaxId = maxId;
	}
	
	@Override
	public boolean test(Release d) {
		return d.getId() > rMaxId;
	}
}
