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

import de.tgmz.discogs.domain.Artist;

public class ArtistCsvPersister extends AbstractCsvPersister<Artist> {
	public ArtistCsvPersister(String target) {
		super(target
			,Table.Artist
			, Table.artist_aliases, Table.artist_groups, Table.artist_members
			, Table.Artist_variations, Table.artist_aliases_all, Table.artist_groups_all
			, Table.artist_members_all);
	}
	
	protected int doSave(Artist a) throws IOException {
		m.get(Table.Artist).printRecord(
			a.getDataQuality().ordinal()
			, a.getId()
			, a.getName()
			, a.getRealname()
		);
		
		for (Artist aa : a.getAliases()) {
			m.get(Table.artist_aliases).printRecord(
				a.getId()
				, aa.getId()
			);
			
			m.get(Table.artist_aliases_all).printRecordUsingCache(
					aa.getId()
					, aa.getName()
			);
		}
		
		for (Artist ag : a.getGroups()) {
			m.get(Table.artist_groups).printRecord(
				a.getId()
				, ag.getId()
			);
			
			m.get(Table.artist_groups_all).printRecordUsingCache(
					ag.getId()
					, ag.getName()
			);
		}
		
		for (Artist am : a.getMembers()) {
			m.get(Table.artist_members).printRecord(
				a.getId()
				, am.getId()
			);
			
			m.get(Table.artist_members_all).printRecordUsingCache(
					am.getId()
					, am.getName()
			);
		}
		
		for (String v : a.getVariations()) {
			m.get(Table.Artist_variations).printRecord(
				a.getId()
				, v
			);
		}
		
		return 1;
	}
}
