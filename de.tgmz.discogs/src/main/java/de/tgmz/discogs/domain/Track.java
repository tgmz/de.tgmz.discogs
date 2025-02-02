/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
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
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Transient;

/**
 * Track entity
 */
@Entity
public class Track implements Serializable {
	@Transient
	private static final long serialVersionUID = 5684918391708831387L;
	@Id
	@GeneratedValue
	private long id;
	private String title;
	private String position;
	private String duration;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Artist> artists;
	@Transient
	private List<Long> artistIds;

	public Track() {
		artistIds = new LinkedList<>();
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getPosition() {
		return position;
	}

	public String getDuration() {
		return duration;
	}

	public Set<Artist> getArtists() {
		return artists;
	}

	/**
	 * The ids of the artists involved. Useful in ReleaseContentHandler if we decide not to persist a release.
	 * @return the artists
	 */
	public List<Long> getArtistIds() {
		return artistIds;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setArtists(Set<Artist> artists) {
		this.artists = artists;
	}

	public void setArtistIds(List<Long> artistIds) {
		this.artistIds = artistIds;
	}

	@Override
	public String toString() {
		return "Track [position=" + position + ", title=" + title + ", duration=" + duration + "]";
	}
}