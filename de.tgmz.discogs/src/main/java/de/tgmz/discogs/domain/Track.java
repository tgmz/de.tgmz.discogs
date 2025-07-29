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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

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
	// "sequence" is important for ordering in Release.class. Ordering by postion is impossible due to its inconsitent format and
	// trackNumber is 0 on headlines which crashes ordering on albums with more than one headline.
	private int sequence;
	private int trackNumber;
	private String title;
	private String position;
	private String duration;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private List<Artist> artists;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<ExtraArtist> extraArtists;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@OrderBy(value = "trackNumber")
	private List<SubTrack> subTracklist;

	public Track() {
		subTracklist = new LinkedList<>();
		artists = new LinkedList<>();
		extraArtists = new LinkedList<>();
	}
	
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

	public int getSequence() {
		return sequence;
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

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * Compute the amount of information this track carries
	 * @return A measure for the amount of information this track carries
	 */
	public int sizeOf() {
		int res = Math.max(1, subTracklist.size()) * extraArtists.size();
		
		res += getSubTracklist().stream().map(SubTrack::sizeOf).reduce(0, Integer::sum);
		
		return res;
	}

	/**
	 * Computes if the ExatraArtist applies to a track.
	 * @param eat the ExatraArtist (unused) and the tracks it applies to
	 */
	public boolean isApplicable(String tracks) {
		if (StringUtils.isEmpty(tracks)) {
			return true;
		}
		
		if (this.position == null) {
			return false;
		}
		
		String[] split = tracks.split("\\s*,\\s*");		// e.g. "A1 to A5, B2 to B9"

		if (Strings.CS.containsAny(this.position, split)) {	// Obvious
			return true;
		}
		
		boolean applicable = false;
		Iterator<String> it = Arrays.asList(split).iterator();
		
		while (it.hasNext() && !applicable) {
			String[] range = it.next().split("\\sto\\s*");	// e.g. "A1 to A3"
			
			if (range.length == 2) {
				applicable = range[0].compareTo(this.position) <= 0 && range[1].compareTo(this.position) >= 0;  
			}
		}
		
		return applicable;
	}
	
	@Override
	public String toString() {
		return "Track [id=" + id + ", sequence=" + sequence + ", trackNumber=" + trackNumber + ", position=" + position + ", title="
				+ title + ", duration=" + duration + ", artists=" + artists + ", extraArtists=" + extraArtists
				+ ", subTracklist=" + subTracklist + "]";
	}
}