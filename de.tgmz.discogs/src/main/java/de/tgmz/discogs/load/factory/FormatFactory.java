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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import de.tgmz.discogs.domain.Format;
import jakarta.persistence.EntityManager;

public class FormatFactory implements IFactory<Format> {
	private LoadingCache<Format, Format> formatCache;
	
	public FormatFactory() {
		formatCache = Caffeine.newBuilder().build(new CacheLoader<Format, Format>() {
			@Override
			public Format load(Format key) {
				return key;
			}
		});
	}
	
	@Override
	public Format get(EntityManager em, Format draft) {
		return formatCache.get(draft, a -> draft);
	}
}
