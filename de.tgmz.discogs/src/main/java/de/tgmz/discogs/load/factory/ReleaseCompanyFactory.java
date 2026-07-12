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

import de.tgmz.discogs.domain.Company;
import de.tgmz.discogs.domain.EntityType;
import de.tgmz.discogs.domain.ReleaseCompany;
import jakarta.persistence.EntityManager;

public class ReleaseCompanyFactory implements IFactory<ReleaseCompany> {
	private IFactory<Company> cf;
	private IFactory<EntityType> etf;
	
	public ReleaseCompanyFactory() {
		cf = new BasicEntityFactory<>();
		etf = new BasicEntityFactory<>();
	}
	
	@Override
	public ReleaseCompany get(EntityManager em, ReleaseCompany draft) {
		draft.setCompany(cf.get(em, draft.getCompany()));
		draft.setEntityType(etf.get(em, draft.getEntityType()));
		
		return draft;
	}
}
