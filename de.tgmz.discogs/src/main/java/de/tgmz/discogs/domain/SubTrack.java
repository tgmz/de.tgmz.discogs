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
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Transient;

/**
 * SubTrack entity
 */
@Entity
public class SubTrack implements Serializable {
	@Transient
	private static final long serialVersionUID = 5772183040087284559L;
	@Id
	@GeneratedValue
	private long id;
	private int subTrackNumber;
	private String title;
	@Column(length = 32)
	private String position;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<ExtraArtist> extraArtists;
	@Column(length = 16)
	private String duration;

	public SubTrack() {
		extraArtists = new HashSet<>();
	}
	public int getSubTrackNumber() {
		return subTrackNumber;
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
	
	public void setId(long id) {
		this.id = id;
	}

	public void setSubTrackNumber(int subTrackNumber) {
		this.subTrackNumber = subTrackNumber;
	}

	public void setTitle(String name) {
		this.title = name;
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