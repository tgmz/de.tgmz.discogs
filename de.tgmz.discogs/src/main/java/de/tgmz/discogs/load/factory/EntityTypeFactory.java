/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import de.tgmz.discogs.domain.EntityType;
import jakarta.persistence.EntityManager;

public class EntityTypeFactory implements IFactory<EntityType> {
	@Override
	public EntityType get(EntityManager em, EntityType draft) {
		EntityType et = em.find(EntityType.class, draft.getId());
		
		if (et == null) {
			et = draft;
			
			em.persist(et);
		}
		
		return et;
	}
}
