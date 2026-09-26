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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

/**
 * SubTrack entity
 */
@Entity
@Table(indexes = {
	@Index(columnList = "title", name = "SubTrack_title_idx"), 
})

public class SubTrack implements Serializable {
	@Transient
	private static final long serialVersionUID = 5772183040087284559L;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "subtrack_gen")
	@TableGenerator(name = "subtrack_gen", allocationSize = 1, initialValue = 1)
	private long id;
	@Column(length = 512)
	private String title;
	private String position;
	private short subTrackNumber;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "SubTrack_ExtraArtist", indexes = {
		@Index(columnList = "SubTrack_id, extraArtists_artist_id, extraArtists_role_id", name = "SubTrack_ExtraArtist_pkey", unique = true),
	})
	private Set<ExtraArtist> extraArtists;
	private String duration;
	@ManyToOne
	private Track track;

	public SubTrack() {
		extraArtists = new HashSet<>();
	}

	public SubTrack(Track track) {
		this();
		
		this.track = track;
	}

	public long getId() {
		return id;
	}
	
	public short getSubTrackNumber() {
		return subTrackNumber;
	}

	public String getTitle() {
		return title;
	}

	public String getPosition() {
		return position;
	}

	public Collection<ExtraArtist> getExtraArtists() {
		return extraArtists;
	}

	public String getDuration() {
		return duration;
	}
	
	public void setSubTrackNumber(short subTrackNumber) {
		this.subTrackNumber = subTrackNumber;
	}

	public void setTitle(String name) {
		this.title = StringUtils.left(name, 512);
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setExtraArtists(Collection<ExtraArtist> extraArtists) {
		this.extraArtists = new HashSet<>(extraArtists);
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