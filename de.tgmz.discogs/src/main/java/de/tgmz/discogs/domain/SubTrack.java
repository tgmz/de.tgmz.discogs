/*********************************************************************
* Copyright (c) 12.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.tgmz.discogs.domain.id.SubTrackId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * SubTrack entity
 */
@Entity
@Table(indexes = {
	@Index(columnList = "track_release_id,track_sequence,subTrackNumber", name = "SubTrack_pkey", unique = true),
	@Index(columnList = "title", name = "SubTrack_title_idx"), 
})

public class SubTrack implements IIdentifiable<SubTrackId> {
	@Transient
	private static final long serialVersionUID = 5772183040087284559L;
	@EmbeddedId
	private SubTrackId id;
	@Column(length = 512)
	private String title;
	private String position;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "SubTrack_ExtraArtist"
	, indexes = {
		@Index(columnList = "SubTrack_track_release_id,SubTrack_track_sequence,SubTrack_subTrackNumber,extraArtists_role,extraArtists_artist_id", name = "SubTrack_ExtraArtist_pkey", unique = true),
	})
	private Set<ExtraArtist> extraArtists;
	private String duration;

	public SubTrack() {
		id = new SubTrackId();
		
		extraArtists = new HashSet<>();
	}

	public SubTrack(Track t) {
		this();
		
		id.setTrack(t);
	}
	
	@Override
	public SubTrackId getId() {
		return id;
	}
	
	public short getSubTrackNumber() {
		return id.getSubTrackNumber();
	}

	public String getTitle() {
		return title;
	}

	public String getPosition() {
		return position;
	}

	public Set<ExtraArtist> getExtraArtists() {
		return extraArtists;
	}

	public String getDuration() {
		return duration;
	}
	
	public void setId(SubTrackId id) {
		this.id = id;
	}

	public void setSubTrackNumber(short subTrackNumber) {
		this.id.setSubTrackNumber(subTrackNumber);
	}

	public void setTitle(String name) {
		this.title = StringUtils.left(name, 512);
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setExtraArtists(Set<ExtraArtist> extraArtists) {
		this.extraArtists = extraArtists;
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * Compute the amount of information this track carries
	 * @return A measure for the amount of information this track carries
	 */
	public int sizeOf() {
		return extraArtists.size();
	}

	@Override
	public String toString() {
		return "SubTrack [position=" + position + ", title=" + title + "]";
	}
}