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

import java.util.Collection;
import java.util.LinkedList;

import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;

/**
 * Utilityclass to replace all elements of a java.util.Set
 * @param <T> the type of the sets element
 */
public class CollectionFactory<T> {
	private IFactory<T> factory;
	
	public CollectionFactory(IFactory<T> factory) {
		this.factory = factory;
	}

	public Collection<T> replaceAll(EntityManager em, Collection<T> param) {
		Collection<T> s = new LinkedList<>();
		
		param.forEach(t -> addIfNotNull(em, s, t));
		
		return s;
	}
	
	private void addIfNotNull(EntityManager em, Collection<T> s, T t) {
		T t0 = factory.get(em, t);
		
		if (t0 != null) {
			s.add(t0);
		}
	}
}
