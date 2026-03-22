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

import de.tgmz.discogs.domain.Style;
import jakarta.persistence.EntityManager;

public class StyleFactory implements IFactory<Style> {
	private static Map<String, Style> cache = new TreeMap<>(); 
	
	@Override
	public Style get(EntityManager em, Style draft) {
		return cache.computeIfAbsent(draft.getId(), s -> draft);
	}
}
