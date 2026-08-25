/*********************************************************************
* Copyright (c) 19.08.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import de.tgmz.discogs.domain.Format;
import de.tgmz.discogs.domain.FormatName;
import jakarta.persistence.EntityManager;

public class FormatFactory implements IFactory<Format> {
	private BasicEntityFactory<FormatName> bffn;

	public FormatFactory() {
		bffn = new BasicEntityFactory<>();
	}
	
	@Override
	public Format get(EntityManager em, Format draft) {
		draft.setName(bffn.get(em, draft.getName()));
		
		return draft;
	}
}
