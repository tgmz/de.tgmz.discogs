--*********************************************************************
--* Copyright (c) 14.03.2026 Thomas Zierer
--*
--* This program and the accompanying materials are made
--* available under the terms of the Eclipse Public License 2.0
--* which is available at https://www.eclipse.org/legal/epl-2.0/
--*
--* SPDX-License-Identifier: EPL-2.0
--**********************************************************************/
create table Artist (data_quality smallint check ((data_quality between 0 and 5)), id integer not null, name varchar(255), realname varchar(255), primary key (id))
create table artist_aliases (Artist_id integer not null, aliases_id integer not null, primary key (Artist_id, aliases_id))
create table artist_groups (Artist_id integer not null, groups_id integer not null, primary key (Artist_id, groups_id))
create table artist_members (Artist_id integer not null, members_id integer not null, primary key (Artist_id, members_id))
create table Artist_variations (Artist_id integer not null, variations varchar(255), unique (Artist_id, variations))
create table Company (id integer not null, name varchar(255), primary key (id))
create table EntityType (id smallint not null, name varchar(255), primary key (id))
create table ExtraArtist (artist_id integer not null, detail_id varchar(255) not null, role_id varchar(255) not null, constraint ExtraArtist_pkey primary key (role_id, detail_id, artist_id))
create table Format (id integer not null, qty float(24), name varchar(31), text varchar(255), primary key (id))
create table Format_descriptions (Format_id integer not null, descriptions varchar(255), unique (Format_id, descriptions))
create table format_gen (next_val bigint, sequence_name varchar(255) not null, primary key (sequence_name))
insert into format_gen(sequence_name, next_val) values ('Format',1)
create table FormatName (id varchar(31) not null, primary key (id))
create table Genre (id varchar(31) not null, primary key (id))
create table Identifier (_type smallint check ((_type between 0 and 13)), id integer not null, _description varchar(255), _value varchar(255), primary key (id))
create table identifier_gen (next_val bigint, sequence_name varchar(255) not null, primary key (sequence_name))
insert into identifier_gen(sequence_name, next_val) values ('Identifier',1)
create table Label (data_quality smallint check ((data_quality between 0 and 5)), id integer not null, parentLabel_id integer, name varchar(255), primary key (id))
create table Label_Label (Label_id integer not null, subLabels_id integer not null, primary key (Label_id, subLabels_id))
create table Master (data_quality smallint check ((data_quality between 0 and 5)), id integer not null, published integer, albumArtist varchar(512), title varchar(255), primary key (id))
create table Master_Artist (Master_id integer not null, artists_id integer not null, primary key (Master_id, artists_id))
create table Master_Genre (Master_id integer not null, genres_id varchar(31) not null, primary key (Master_id, genres_id))
create table Master_Style (Master_id integer not null, styles_id varchar(31) not null, primary key (Master_id, styles_id))
create table Release (_main boolean not null, data_quality smallint check ((data_quality between 0 and 5)), id integer not null, master_id integer, series_id integer, albumArtist varchar(512), country varchar(255), released varchar(255), title varchar(255), primary key (id))
create table Release_Artist (Release_id integer not null, artists_id integer not null, primary key (Release_id, artists_id))
create table Release_Format (Release_id integer not null, formats_id integer not null unique)
create table Release_Genre (Release_id integer not null, genres_id varchar(31) not null, primary key (Release_id, genres_id))
create table Release_Identifier (Release_id integer not null, identifiers_id integer not null unique)
create table Release_labels (Release_id integer not null, labels_KEY integer not null, catno varchar(255), primary key (Release_id, labels_KEY))
create table Release_Style (Release_id integer not null, styles_id varchar(31) not null, primary key (Release_id, styles_id))
create table Release_Track (Release_id integer not null, tracklist_release_id integer not null, tracklist_sequence_id smallint not null, unique (tracklist_release_id, tracklist_sequence_id))
create table ReleaseCompany (company_id integer not null, entityType_id smallint not null, release_id integer not null, constraint ReleaseCompany_pkey primary key (entityType_id, company_id, release_id))
create table ReleaseExtraArtist (artist_id integer not null, release_id integer not null, detail_id varchar(255) not null, role_id varchar(255) not null, constraint ReleaseExtraArtist_pkey primary key (release_id, role_id, detail_id, artist_id))
create table ReleaseExtraArtist_applicableTracks (ReleaseExtraArtist_artist_id integer not null, ReleaseExtraArtist_release_id integer not null, ReleaseExtraArtist_detail_id varchar(255) not null, ReleaseExtraArtist_role_id varchar(255) not null, applicableTracks varchar(255), unique (ReleaseExtraArtist_artist_id, ReleaseExtraArtist_detail_id, ReleaseExtraArtist_release_id, ReleaseExtraArtist_role_id, applicableTracks))
create table Series (id integer not null, catno varchar(255), name varchar(255), primary key (id))
create table Style (id varchar(31) not null, primary key (id))
create table SubTrack (release_id integer not null, sequence_id smallint not null, subtracknumber_id smallint not null, title varchar(512), duration varchar(255), position varchar(255), primary key (release_id, sequence_id, subtracknumber_id))
create table SubTrack_ExtraArtist (SubTrack_release_id integer not null, SubTrack_sequence_id smallint not null, SubTrack_subtracknumber_id smallint not null, extraArtists_artist_id integer not null, extraArtists_detail_id varchar(255) not null, extraArtists_role_id varchar(255) not null, constraint SubTrack_ExtraArtist_pkey unique (SubTrack_release_id, SubTrack_sequence_id, SubTrack_subtracknumber_id, extraArtists_artist_id, extraArtists_role_id, extraArtists_detail_id))
create table Track (release_id integer not null, sequence_id smallint not null, trackNumber smallint not null, title varchar(512), duration varchar(255), position varchar(255), primary key (release_id, sequence_id))
create table Track_Artist (Track_release_id integer not null, Track_sequence_id smallint not null, artists_id integer not null, constraint Track_Artist_pkey unique (Track_release_id, Track_sequence_id, artists_id))
create table Track_ExtraArtist (Track_release_id integer not null, Track_sequence_id smallint not null, extraArtists_artist_id integer not null, extraArtists_detail_id varchar(255) not null, extraArtists_role_id varchar(255) not null, constraint Track_ExtraArtist_pkey unique (Track_release_id, Track_sequence_id, extraArtists_artist_id, extraArtists_role_id, extraArtists_detail_id))
create table Track_SubTrack (Track_release_id integer not null, Track_sequence_id smallint not null, subTracklist_release_id integer not null, subTracklist_sequence_id smallint not null, subTracklist_subtracknumber_id smallint not null, unique (subTracklist_release_id, subTracklist_sequence_id, subTracklist_subtracknumber_id))
create index Artist_name_idx on Artist (name)
create index Artist_variations_name_idx on Artist_variations (variations)
create index Company_name_idx on Company (name)
create index Label_name_idx on Label (name)
create index Master_title_idx on Master (title)
create index Master_albumArtist_idx on Master (albumArtist)
create index Release_albumArtist_idx on Release (albumArtist)
create index Release_title_idx on Release (title)
create index Series_name_idx on Series (name)
create index SubTrack_title_idx on SubTrack (title)
create index Track_title_idx on Track (title)
alter table if exists artist_aliases add constraint FK51lsn2wy2ma5fyb67roffvpkk foreign key (aliases_id) references Artist
alter table if exists artist_aliases add constraint FKelhahxktvecoipysvk9xjhug7 foreign key (Artist_id) references Artist
alter table if exists artist_groups add constraint FK1kq8p1rnbcqu0duxsajtmahem foreign key (groups_id) references Artist
alter table if exists artist_groups add constraint FKh3pwqulw5qrr8sooangxl6cw4 foreign key (Artist_id) references Artist
alter table if exists artist_members add constraint FKrjgdr26d3xnj2xu9ayxv2cae2 foreign key (members_id) references Artist
alter table if exists artist_members add constraint FKiyrt8iqdbp51nqh55kr19x5n3 foreign key (Artist_id) references Artist
alter table if exists Artist_variations add constraint FKrki9786wlsmogjq85ssqll25j foreign key (Artist_id) references Artist
alter table if exists ExtraArtist add constraint FK4dgigpw7yg0pkadbpitorqkaj foreign key (artist_id) references Artist
alter table if exists Format add constraint FKns8n4o60yvpdbxtybn1kooukc foreign key (name) references FormatName
alter table if exists Format_descriptions add constraint FKduoc80owico3gub8yr6qokedi foreign key (Format_id) references Format
alter table if exists Label add constraint FKdecmav21lxstcu445twxnghd8 foreign key (parentLabel_id) references Label
alter table if exists Label_Label add constraint FKcsnyi4muwxxgtjlehg6hjgbiq foreign key (subLabels_id) references Label
alter table if exists Label_Label add constraint FK3fnkr733992ywq1wd0poxxxbv foreign key (Label_id) references Label
alter table if exists Master_Artist add constraint FKsjc60rjhpsy6u3lpuh94ekco7 foreign key (artists_id) references Artist
alter table if exists Master_Artist add constraint FKe5ks2nepjou9dxjya4uv7a6m1 foreign key (Master_id) references Master
alter table if exists Master_Genre add constraint FKsi7jqsjajj0gyi1o1icwka217 foreign key (genres_id) references Genre
alter table if exists Master_Genre add constraint FKhlqekntv1a4quc5mtd1yuty39 foreign key (Master_id) references Master
alter table if exists Master_Style add constraint FKith9t44bcfuw7y4reh6jv08pb foreign key (styles_id) references Style
alter table if exists Master_Style add constraint FKdfuln42s0ie5cpki8p762hrb1 foreign key (Master_id) references Master
alter table if exists Release add constraint FK8gixt7pw3n2amghs1nemj3mcm foreign key (master_id) references Master
alter table if exists Release add constraint FK3hr6u3mck4xpt1ya4sftjbohc foreign key (series_id) references Series
alter table if exists Release_Artist add constraint FKmfjrsxqbuey6t1pvlo2txe2x0 foreign key (artists_id) references Artist
alter table if exists Release_Artist add constraint FKgqgy8p1cuooxlyu04229f7u3c foreign key (Release_id) references Release
alter table if exists Release_Format add constraint FK7xusesury62xvm0sb24xw66y8 foreign key (formats_id) references Format
alter table if exists Release_Format add constraint FKmqmurqv4284ssyx79dofa8oln foreign key (Release_id) references Release
alter table if exists Release_Genre add constraint FK4vks82y2nynvveo0kftbgbrnr foreign key (genres_id) references Genre
alter table if exists Release_Genre add constraint FKm9o9ge8knafrmtmu6k35u8sxd foreign key (Release_id) references Release
alter table if exists Release_Identifier add constraint FKrnl2qlko71sq2raamr6tgh6tn foreign key (identifiers_id) references Identifier
alter table if exists Release_Identifier add constraint FKbtl3xr934xbca7wtoa9r3cwru foreign key (Release_id) references Release
alter table if exists Release_labels add constraint FKa7dyo3m3in0g3hb6gjlvruuga foreign key (labels_KEY) references Label
alter table if exists Release_labels add constraint FKht4lrxrosuqi1qb0c1j6qijbq foreign key (Release_id) references Release
alter table if exists Release_Style add constraint FKkfpgqf0qfpjub3px2h8w05rlf foreign key (styles_id) references Style
alter table if exists Release_Style add constraint FKvwwi9puhw46ahpjpm6uwho6v foreign key (Release_id) references Release
alter table if exists Release_Track add constraint FKkp2mnt3q268vaqvufkh93eun2 foreign key (tracklist_release_id, tracklist_sequence_id) references Track
alter table if exists Release_Track add constraint FKbdrgn76mq2leno9i4rtsnj3io foreign key (Release_id) references Release
alter table if exists ReleaseCompany add constraint FKajay9cuemxly79edjubbob5mo foreign key (company_id) references Company
alter table if exists ReleaseCompany add constraint FKkbbda4s5kpln0701770b0hcx5 foreign key (entityType_id) references EntityType
alter table if exists ReleaseCompany add constraint FK1n7oo5bny9sqcturto3hcae2y foreign key (release_id) references Release
alter table if exists ReleaseExtraArtist add constraint FKk19qmr9s9fty3plt1panq9moo foreign key (role_id, detail_id, artist_id) references ExtraArtist
alter table if exists ReleaseExtraArtist add constraint FKfme91jq80b289g20f2gafy3ie foreign key (release_id) references Release
alter table if exists ReleaseExtraArtist_applicableTracks add constraint FKk3fefhjai2rh92cqi3m7jpe4u foreign key (ReleaseExtraArtist_release_id, ReleaseExtraArtist_role_id, ReleaseExtraArtist_detail_id, ReleaseExtraArtist_artist_id) references ReleaseExtraArtist
alter table if exists SubTrack add constraint FKpkpk6i9u43vklkm5yn1ud8cs1 foreign key (release_id, sequence_id) references Track
alter table if exists SubTrack_ExtraArtist add constraint FKhn82482a4hiwhk1ueinh9klrd foreign key (extraArtists_role_id, extraArtists_detail_id, extraArtists_artist_id) references ExtraArtist
alter table if exists SubTrack_ExtraArtist add constraint FKnoqjhohxddm663aiopb4kwfp1 foreign key (SubTrack_release_id, SubTrack_sequence_id, SubTrack_subtracknumber_id) references SubTrack
alter table if exists Track add constraint FKohl1c5ugxv1i99qfa59j3rkfd foreign key (release_id) references Release
alter table if exists Track_Artist add constraint FKi4ejqu0y9tder10sw8udlrwtf foreign key (artists_id) references Artist
alter table if exists Track_Artist add constraint FKv30r4dqkl8510vsuc2ly8mp9 foreign key (Track_release_id, Track_sequence_id) references Track
alter table if exists Track_ExtraArtist add constraint FKsc8k6bio393j97ik1cm18eay1 foreign key (extraArtists_role_id, extraArtists_detail_id, extraArtists_artist_id) references ExtraArtist
alter table if exists Track_ExtraArtist add constraint FK4uck3nm5yhj6kxxsbmuph2abw foreign key (Track_release_id, Track_sequence_id) references Track
alter table if exists Track_SubTrack add constraint FK8no15jxh3apq4iab9vg03xaed foreign key (subTracklist_release_id, subTracklist_sequence_id, subTracklist_subtracknumber_id) references SubTrack
alter table if exists Track_SubTrack add constraint FKlylbnl3ldil6yfmtuf8624ypx foreign key (Track_release_id, Track_sequence_id) references Track
