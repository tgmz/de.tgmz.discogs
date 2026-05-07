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
import de.tgmz.discogs.domain.Genre;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.Style;

public class MasterCsvPersister extends AbstractCsvPersister<Master> {
	public MasterCsvPersister(String target) {
		super(target
			, Table.Master, Table.Master_Artist, Table.Genre, Table.Style, Table.Master_Genre, Table.Master_Style, Table.artist_master_all);
	}
	
	protected int doSave(Master l) throws IOException {
		m.get(Table.Master).printRecord(
			l.getDataQuality().ordinal()
			, l.getPublished()
			, l.getId()
			, l.getAlbumArtist()
			, l.getTitle()
		);
		
		for (Artist a : l.getArtists()) {
			m.get(Table.Master_Artist).printRecord(
				l.getId()
				, a.getId()
			);
			
			m.get(Table.artist_master_all).printRecordUsingCache(
					a.getId()
					, a.getName()
			);
		}
		
		for (Genre g : l.getGenres()) {
			m.get(Table.Master_Genre).printRecord(
				l.getId()
				, g.getId()
			);
		}

		for (Style s : l.getStyles()) {
			m.get(Table.Master_Style).printRecord(
				l.getId()
				, s.getId()
			);
		}
		
		return 1;
	}
}
