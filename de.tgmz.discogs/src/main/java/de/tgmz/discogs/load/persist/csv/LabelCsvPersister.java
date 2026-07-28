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
		super(target
			, Table.Label
			, Table.Label_Label
			, Table.label_label_all.useCache(300_000));
	}
	protected int doSave(Label l) throws IOException {
		Label pl = l.getParentLabel();
		
		Integer pls = pl != null ? pl.getId() : null;  
		
		pm.get(Table.Label).printRecord(
			l.getDataQuality().ordinal()
			, l.getId()
			, pls
			, l.getName()
			, l.getContactinfo()
		);
		
		if (pl != null) {
			pm.get(Table.label_label_all).printRecordUsingCache(
				pl.getId()
				, pl.getName()
			);
		}
		
		for (Label sl : l.getSubLabels()) {
			pm.get(Table.Label_Label).printRecord(
				l.getId()
				, sl.getId()
			);
			
			pm.get(Table.label_label_all).printRecordUsingCache(
				sl.getId()
				, sl.getName()
			);
			
		}
		
		return 1;
	}
}
