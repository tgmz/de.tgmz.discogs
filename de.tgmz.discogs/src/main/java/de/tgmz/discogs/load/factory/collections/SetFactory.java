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

import java.util.HashSet;
import java.util.Set;

import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;

/**
 * Utilityclass to replace all elements of a java.util.Set
 * @param <T> the type of the sets element
 */
public class SetFactory<T> {
	private EntityManager em;
	private IFactory<T> factory;
	
	public SetFactory(EntityManager em, IFactory<T> factory) {
		this.em = em;
		this.factory = factory;
	}

	public Set<T> replaceAll(Set<T> param) {
		Set<T> s = new HashSet<>();
		
		param.stream().forEach(t -> addIfNotNull(s, t));
		
		return s;
	}
	
	private void addIfNotNull(Set<T> s, T t) {
		T t0 = factory.get(em, t);
		
		if (t0 != null) {
			s.add(t0);
		}
	}
}
