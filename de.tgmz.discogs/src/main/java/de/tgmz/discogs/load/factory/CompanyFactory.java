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

import de.tgmz.discogs.domain.Company;
import jakarta.persistence.EntityManager;

public class CompanyFactory implements IFactory<Company> {
	@Override
	public Company get(EntityManager em, Company draft) {
		Company c = em.find(Company.class, draft.getId());
		
		if (c == null) {
			c = draft;
			
			em.persist(c);
		}
		
		return c;
	}
}
