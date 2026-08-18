/*********************************************************************
* Copyright (c) 06.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.csv;

import java.util.Collections;
import java.util.List;

public enum Table implements ITable {
	Artist
	, artist_artist_all
	, artist_aliases
	, artist_groups
	, artist_members
	, Artist_variations
	, Master
	, label_label_all
	, Label
	, Label_Label
	, Genre
	, Master_Genre
	, Style
	, Master_Style
	, artist_master_all
	, Master_Artist
	, Series
	, Release
	, Release_Genre
	, Release_Style
	, label_release_all
	, Release_labels
	, artist_release_all
	, Release_Artist
	, EntityType
	, Company
	, ReleaseCompany
	, format_gen
	, Format
	, Format_descriptions
	, Release_Format
	, ExtraArtist
	, ReleaseExtraArtist
	, ReleaseExtraArtist_applicableTracks
	, Track
	, Track_Artist
	, Track_ExtraArtist
	, Release_Track
	, SubTrack
	, SubTrack_ExtraArtist
	, Track_SubTrack
	;
	
	private int cacheSize = -1;

	@Override
	public List<ITable> dependsOn() {
		return Collections.emptyList();
	}
	
	public int getCacheSize() {
		return cacheSize;
	}

	public Table useCache(int cacheSize) {
		this.cacheSize = cacheSize;
		
		return this;
	}
}
