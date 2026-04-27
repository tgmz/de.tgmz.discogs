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
			, "Artist", "artist_aliases", "artist_groups", "artist_members", "Artist_variations", "artist_aliases_all", "artist_groups_all", "artist_members_all");
	}
	
	protected void doSave(Artist r) throws IOException {
		m.get("Artist").printRecord(
			r.getDataQuality().ordinal()
			, r.getId()
			, r.getName()
			, r.getRealname()
		);
		
		for (Artist a : r.getAliases()) {
			m.get("artist_aliases").printRecord(
				r.getId()
				, a.getId()
			);
		
			m.get("artist_aliases_all").printRecord(
					a.getId()
					, a.getName()
			);
		}
		
		for (Artist a : r.getGroups()) {
			m.get("artist_groups").printRecord(
				r.getId()
				, a.getId()
			);
			
			m.get("artist_groups_all").printRecord(
					a.getId()
					, a.getName()
			);
		}
		
		for (Artist a : r.getMembers()) {
			m.get("artist_members").printRecord(
				r.getId()
				, a.getId()
			);
			
			m.get("artist_members_all").printRecord(
					a.getId()
					, a.getName()
			);
		}
		
		for (String v : r.getVariations()) {
			m.get("Artist_variations").printRecord(
				r.getId()
				, v
			);
		}
	}
}
