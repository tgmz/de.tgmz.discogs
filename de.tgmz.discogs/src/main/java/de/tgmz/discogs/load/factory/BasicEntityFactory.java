/*********************************************************************
* Copyright (c) 31.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import de.tgmz.discogs.domain.IIdentifiable;
import jakarta.persistence.EntityManager;

public class BasicEntityFactory<T extends IIdentifiable<?>> implements IFactory<T> {
	@Override
	public T get(EntityManager em, T draft) {
		// There must be a better way other than calling getClass() but I can't figure out one
		@SuppressWarnings("unchecked")
		T t = (T) em.find(draft.getClass(), draft.getId());
		
		if (t == null) {
			t = draft;
			
			em.persist(t);
		}
		
		return t;
	}
}
