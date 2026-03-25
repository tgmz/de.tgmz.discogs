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

import de.tgmz.discogs.domain.Role;
import jakarta.persistence.EntityManager;

public class RoleFactory implements IFactory<Role> {
	@Override
	public Role get(EntityManager em, Role draft) {
		Role r = em.find(Role.class, draft.getId());
		
		if (r == null) {
			r = draft;
			
			em.persist(r);
		}
		
		return r;
	}
}
