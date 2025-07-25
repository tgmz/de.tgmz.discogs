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
import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
	private int trackNumber;
	private String title;
	private String position;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private List<ExtraArtist> extraArtists;

	public SubTrack() {
		extraArtists = new LinkedList<>();
	}
	public int getTrackNumber() {
		return trackNumber;
	}

	public String getTitle() {
		return title;
	}

	public String getPosition() {
		return position;
	}

	public List<ExtraArtist> getExtraArtists() {
		return extraArtists;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public void setPosition(String position) {
		this.position = position;
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