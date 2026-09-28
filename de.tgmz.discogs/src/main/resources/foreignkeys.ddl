--*********************************************************************
--* Copyright (c) 28.09.2026 Thomas Zierer
--*
--* This program and the accompanying materials are made
--* available under the terms of the Eclipse Public License 2.0
--* which is available at https://www.eclipse.org/legal/epl-2.0/
--*
--* SPDX-License-Identifier: EPL-2.0
--**********************************************************************/
--*
--* Additional foreign keys to simplify navigation
--*
alter table if exists ReleaseExtraArtist add constraint FK_ReleaseExtraArtist_Artist foreign key (artist_id) references Artist
alter table if exists ReleaseExtraArtist_applicableTracks add constraint FK_ReleaseExtraArtist_applicableTracks_Artist foreign key (ReleaseExtraArtist_artist_id) references Artist
alter table if exists ReleaseExtraArtist_applicableTracks add constraint FK_ReleaseExtraArtist_applicableTracks_Release foreign key (ReleaseExtraArtist_release_id) references Release
alter table if exists SubTrack_ExtraArtist add constraint FK_SubTrack_ExtraArtist_Artist foreign key (extraArtists_artist_id) references Artist
alter table if exists SubTrack_ExtraArtist add constraint FK_SubTrack_ExtraArtist_Release foreign key (SubTrack_release_id) references Release
alter table if exists Track_ExtraArtist add constraint FK_Track_ExtraArtist_Artist foreign key (extraArtists_artist_id) references Artist
alter table if exists Track_ExtraArtist add constraint FK_Track_ExtraArtist_Release foreign key (track_release_id) references Release
