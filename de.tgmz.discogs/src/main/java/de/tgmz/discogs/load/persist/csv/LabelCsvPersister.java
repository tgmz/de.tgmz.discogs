/*********************************************************************
* Copyright (c) 21.04.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.csv;

import java.io.IOException;

import de.tgmz.discogs.domain.Label;

public class LabelCsvPersister extends AbstractCsvPersister<Label> {
	public LabelCsvPersister(String target) {
		super(target, Table.Label);
	}
	protected int doSave(Label l) throws IOException {
		Label pl = l.getParentLabel();
		
		Long pls = pl != null ? pl.getId() : null;  
		
		m.get(Table.Label).printRecord(
			l.getDataQuality().ordinal()
			, l.getId()
			, pls
			, l.getName()
		);
		
		return 1;
	}
}
