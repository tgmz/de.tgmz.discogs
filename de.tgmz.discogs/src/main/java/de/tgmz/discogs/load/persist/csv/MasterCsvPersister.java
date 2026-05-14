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

	protected int doSave(Master m) throws IOException {
		pm.get(Table.Master).printRecord(
			m.getDataQuality().ordinal()
			, m.getId()
			, m.getPublished()
			, m.getAlbumArtist()
			, m.getTitle()
		);
		
		for (Artist a : m.getArtists()) {
			pm.get(Table.Master_Artist).printRecord(
				m.getId()
				, a.getId()
			);
			
			pm.get(Table.artist_master_all).printRecordUsingCache(
					a.getId()
					, a.getName()
			);
		}
		
		for (Genre g : m.getGenres()) {
			pm.get(Table.Master_Genre).printRecord(
				m.getId()
				, g.getId()
			);
		}

		for (Style s : m.getStyles()) {
			pm.get(Table.Master_Style).printRecord(
				m.getId()
				, s.getId()
			);
		}
		
		return 1;
	}
}
