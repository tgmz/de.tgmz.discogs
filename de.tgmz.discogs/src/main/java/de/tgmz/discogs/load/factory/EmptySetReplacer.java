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
import java.util.Set;

import jakarta.persistence.EntityManager;

/**
 * Utilityclass to replace all elements of a java.util.Set
 * @param <T> the type of the sets element
 */
public class EmptySetReplacer<T> extends SetReplacer<T> {
	public EmptySetReplacer(EntityManager em, IFactory<T> factory) {
		super(null, null);
	}

	@Override
	public Set<T> replaceAll(Set<T> param) {
		return new HashSet<>();
	}
}
