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
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
	private int trackNumber;
	private String title;
	private String position;
	private String duration;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private List<Artist> artists;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private List<ExtraArtist> extraArtists;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@OrderBy(value = "trackNumber")
	private List<SubTrack> subTracklist;

	public long getId() {
		return id;
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

	public String getDuration() {
		return duration;
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public List<ExtraArtist> getExtraArtists() {
		return extraArtists;
	}

	public List<SubTrack> getSubTracklist() {
		return subTracklist;
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

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setExtraArtists(List<ExtraArtist> extraArtists) {
		this.extraArtists = extraArtists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

	public void setSubTracklist(List<SubTrack> subTracklist) {
		this.subTracklist = subTracklist;
	}

	@Override
	public String toString() {
		return "Track [position=" + position + ", title=" + title + ", duration=" + duration + ", extraArtists=" + extraArtists + "]";
	}
}