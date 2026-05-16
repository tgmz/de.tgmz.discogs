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

public enum Table {
	Artist
	, artist_aliases_all
	, artist_aliases
	, artist_groups_all
	, artist_groups
	, artist_members_all
	, artist_members
	, Artist_variations
	, Label
	, Master
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
	, Release_labels
	, artist_release_all		
	, Release_Artist
	, EntityType
	, Company
	, release_company
	, format_gen
	, Format
	, Format_descriptions
	, Release_Format
	, identifier_gen
	, Identifier
	, Release_Identifier
	, ExtraArtist
	, artist_release_extraartist_all
	, release_extraartist
	, ReleaseExtraArtist_applicableTracks
	, Track
	, artist_release_track_all
	, Track_Artist
	, artist_release_track_extraartist_all
	, Track_ExtraArtist
	, Release_Track
	, SubTrack
	, artist_release_subtrack_extraartist_all
	, SubTrack_ExtraArtist
	, Track_SubTrack
}
