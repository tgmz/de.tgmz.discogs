/*********************************************************************
* Copyright (c) 19.08.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;

/**
 * Utilityclass to replace all keys of a java.util.Map
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class MapFactory<K,V> {
	private IFactory<K> kFactory;
	private IFactory<V> vFactory;
	
	public MapFactory(IFactory<K> kFactory) {
		this(kFactory, (x,v) -> v);	// Simply return the value 
	}

	public MapFactory(IFactory<K> kFactory, IFactory<V> vFactory) {
		this.kFactory = kFactory;
		this.vFactory = vFactory;
	}

	public Map<K,V> replaceAll(EntityManager em, Map<K,V> param) {
		Map<K,V> m = new HashMap<>();
		
		param.entrySet().forEach(e -> addIfNotNull(em, m, e));
		
		return m;
	}
	
	private void addIfNotNull(EntityManager em, Map<K,V> m, Entry<K, V> e) {
		K k0 = kFactory.get(em, e.getKey());
		
		if (k0 != null) { 
			m.put(k0, vFactory.get(em, e.getValue()));
		}
	}
}
