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

import de.tgmz.discogs.domain.Genre;
import jakarta.persistence.EntityManager;

public class GenreFactory implements IFactory<Genre> {
	private Map<String, Genre> cache;
	
	public GenreFactory() {
		super();
		
		cache = new TreeMap<>();
	}
	
	@Override
	public Genre get(EntityManager em, Genre draft) {
		return cache.computeIfAbsent(draft.getId(), g -> draft);
	}
}
