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

public enum Table implements ITable {
	Artist
	, artist_artist_all(Artist)
	, artist_aliases
	, artist_groups
	, artist_members
	, Artist_variations
	, Master
	, label_label_all
	, Label(label_label_all)
	, Label_Label(Label,label_label_all)
	, Genre
	, Master_Genre(Genre)	// Logical dependency: Master_Genre updates Genre
	, Style
	, Master_Style(Style)
	, artist_master_all(Artist, artist_artist_all)	// Technical dependency: artist_master and artist_artist_all must not run parallel.
													// They both update Artist which could lead to a deadlock
	, Master_Artist
	, Series
	, Release(Series, Master)
	, Release_Genre(Genre, Master_Genre)	// Combined logical and technical dependency
	, Release_Style(Style, Master_Style)
	, label_release_all(Label, label_label_all)
	, Release_labels(Label, label_label_all, label_release_all)
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
	
	private int cacheSize = -1;
	private List<ITable> dependsOn;

	private Table(Table... dependsOn) {
		this.dependsOn = List.of(dependsOn);
	}

	@Override
	public List<ITable> dependsOn() {
		return dependsOn;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public Table useCache(int cacheSize) {
		this.cacheSize = cacheSize;
		
		return this;
	}
}
