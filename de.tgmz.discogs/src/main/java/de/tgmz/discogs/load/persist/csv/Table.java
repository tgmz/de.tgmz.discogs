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

import java.util.List;

public enum Table {
	Artist
	, artist_artist_all(Artist)
	, artist_aliases
	, artist_groups
	, artist_members
	, Artist_variations
	, Master
	, Label
	, Genre
	, Master_Genre(Genre)	// A "real" dependency: Master_Genre updates Genre
	, Style
	, Master_Style(Style)
	, artist_master_all(Artist, artist_artist_all)	// This dependency ensures that artist_master and artist_artist_all do not run parallel.
													// They both update Artist which could lead to a deadlock
	, Master_Artist
	, Series(Label)
	, Release(Series, Master)
	, Release_Genre(Genre, Master_Genre)
	, Release_Style(Style, Master_Style)
	, Release_labels(Label)
	, artist_release_all(Artist, artist_artist_all, artist_master_all)
	, Release_Artist
	, EntityType
	, Company
	, release_company
	, format_gen
	, Format(format_gen)
	, Format_descriptions
	, Release_Format
	, ExtraArtist
	, release_extraartist(ExtraArtist)
	, ReleaseExtraArtist_applicableTracks
	, Track
	, Track_Artist
	, Track_ExtraArtist(ExtraArtist, release_extraartist)
	, Release_Track
	, SubTrack
	, SubTrack_ExtraArtist(ExtraArtist, release_extraartist, Track_ExtraArtist)
	, Track_SubTrack
	;
	
	private boolean useCache = false;
	private List<Table> dependsOn;

	private Table(Table... dependsOn) {
		this.dependsOn = List.of(dependsOn);
	}

	public List<Table> getDependsOn() {
		return dependsOn;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public Table useCache() {
		this.useCache = true;
		
		return this;
	}
}
