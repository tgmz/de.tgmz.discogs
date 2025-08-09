/*********************************************************************
* Copyright (c) 19.08.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;

/**
 * Utilityclass to replace all elements of a java.util.Set
 * @param <T> the type of the sets element
 */
public class SetReplacer<T> {
	private EntityManager em;
	private IFactory<T> factory;
	
	public SetReplacer(EntityManager em, IFactory<T> factory) {
		this.em = em;
		this.factory = factory;
	}

	public Set<T> replaceAll(Set<T> param) {
		List<T> l = new LinkedList<>(param);
		
		l.replaceAll(a -> factory.get(em, a));
		
		return new HashSet<>(l);
	}
}
