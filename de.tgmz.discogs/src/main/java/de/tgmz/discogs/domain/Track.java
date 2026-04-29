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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;

import de.tgmz.discogs.domain.id.TrackId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Track entity
 */
@Entity
@Table(indexes = {
	@Index(columnList = "release_id,sequence", name = "Track_pk_idx", unique = true),
})
public class Track implements Serializable {
	@Transient
	private static final long serialVersionUID = 5684918391708831387L;
	@EmbeddedId
	private TrackId id;
	private short trackNumber;
	@Column(length = 512)
	private String title;
	private String position;
	private String duration;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "Track_Artist"
	, indexes = {
		@Index(columnList = "Track_release_id,Track_sequence,artists_id", name = "Track_Artist_pk_idx", unique = true),
	})
	private Set<Artist> artists;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "Track_ExtraArtist"
	, indexes = {
		@Index(columnList = "Track_release_id,Track_sequence,extraArtists_artist_id,extraArtists_role", name = "Track_ExtraArtist_pk_idx", unique = true),
	})
	private Set<ExtraArtist> extraArtists;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "Track_SubTrack"
	, indexes = {
		@Index(columnList = "subTracklist_track_release_id,subTracklist_track_sequence,subTracklist_subTrackNumber", name = "Track_SubTrack_pk_idx", unique = true),
	})
	@OrderBy(value = "subTrackNumber")
	private List<SubTrack> subTracklist;

	public Track() {
		id = new TrackId();
		
		subTracklist = new LinkedList<>();
		artists = new HashSet<>();
		extraArtists = new HashSet<>();
	}
	
	public Track(Release r) {
		this();
		
		id.setRelease(r);
	}
	
	public TrackId getId() {
		return id;
	}

	public short getTrackNumber() {
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

	public Set<Artist> getArtists() {
		return artists;
	}

	public Set<ExtraArtist> getExtraArtists() {
		return extraArtists;
	}

	public List<SubTrack> getSubTracklist() {
		return subTracklist;
	}

	public short getSequence() {
		return id.getSequence();
	}

	public void setId(TrackId id) {
		this.id = id;
	}

	public void setTrackNumber(short trackNumber) {
		this.trackNumber = trackNumber;
	}

	public void setTitle(String name) {
		this.title = StringUtils.left(name, 512);
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setExtraArtists(Set<ExtraArtist> extraArtists) {
		this.extraArtists = extraArtists;
	}

	public void setArtists(Set<Artist> artists) {
		this.artists = artists;
	}

	public void setSequence(short sequence) {
		this.id.setSequence(sequence);
	}

	/**
	 * Compute the amount of information this track carries
	 * @return A measure for the amount of information this track carries
	 */
	public int sizeOf() {
		int res = Math.max(1, subTracklist.size()) * extraArtists.size();
		
		res += artists.size();
		
		res += getSubTracklist().stream().map(SubTrack::sizeOf).reduce(0, Integer::sum);
		
		return res;
	}

	/**
	 * Computes if the ExatraArtist applies to this track.
	 * @param ea the ExatraArtist
	 */
	public boolean isApplicable(Set<String> tracks) {
		if (tracks == null || tracks.isEmpty()) {
			return true;
		}
		
		if (this.position == null) {
			return false;
		}
		
		if (Strings.CS.containsAny(this.position, tracks.toArray(new String[tracks.size()]))) {	// Obvious
			return true;
		}
		
		boolean applicable = false;
		Iterator<String> it = tracks.iterator();
		
		while (it.hasNext() && !applicable) {
			String[] range = it.next().split("\\sto\\s*");	// e.g. "A1 to A3"
			
			if (range.length == 2) {
				applicable = isApplicable(range[0], range[1], this.position);
			}
		}
		
		return applicable;
	}

	private boolean isApplicable(String lowerBound, String upperBound, String position) {
		if (NumberUtils.isCreatable(lowerBound) && NumberUtils.isCreatable(upperBound) && NumberUtils.isCreatable(position)) {
			// Handle positions like "1.1"
			float numericPosition = NumberUtils.createFloat(position);
			
			return NumberUtils.createFloat(lowerBound) <= numericPosition
					&& NumberUtils.createFloat(upperBound) >= numericPosition;
		} else {
			return lowerBound.compareTo(this.position) <= 0 && upperBound.compareTo(position) >= 0;
		}
	}
	
	@Override
	public String toString() {
		return "Track [id=" + id + ", trackNumber=" + trackNumber + ", position=" + position + ", title="
				+ title + ", duration=" + duration + ", artists=" + artists + ", extraArtists=" + extraArtists
				+ ", subTracklist=" + subTracklist + "]";
	}
}