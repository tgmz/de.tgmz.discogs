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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import de.tgmz.discogs.domain.id.TrackKey;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Track entity
 */
@Entity
@Table(indexes = {
	@Index(columnList = "title", name = "Track_title_idx"), 
})
public class Track implements Serializable {
	@Transient
	private static final long serialVersionUID = 5684918391708831387L;

	@Transient
	private static final Pattern P = Pattern.compile("(\\d+)");
	
	@EmbeddedId
	private TrackKey id;
	private short trackNumber;
	@Column(length = 512)
	private String title;
	private String position;
	private String duration;
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "Track_Artist", indexes = {
		@Index(columnList = "Track_release_id, Track_sequence_id, artists_id", name = "Track_Artist_pkey", unique = true),
	})
	private Set<Artist> artists;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "Track_ExtraArtist", indexes = {
		@Index(columnList = "Track_release_id, Track_sequence_id, extraArtists_artist_id, extraArtists_credit_id, extraArtists_detail_id", name = "Track_ExtraArtist_pkey", unique = true),
	})
	private Set<ExtraArtist> extraArtists;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy(value = "subtracknumber_id")
	private List<SubTrack> subTracklist;
	@ManyToOne
	@MapsId("releaseId")
	@JoinColumn(name = "release_id")
	private Release release;

	public Track() {
		id = new TrackKey();
		
		subTracklist = new LinkedList<>();
		artists = new HashSet<>();
		extraArtists = new HashSet<>();
	}
	
	public Track(Release release) {
		this();
		
		this.release = release;
		this.id.setReleaseId(release.getId());
	}

	public TrackKey getId() {
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

	public Collection<Artist> getArtists() {
		return artists;
	}

	public Collection<ExtraArtist> getExtraArtists() {
		return extraArtists;
	}

	public List<SubTrack> getSubTracklist() {
		return subTracklist;
	}

	public short getSequence() {
		return id.getSequenceId();
	}

	public Release getRelease() {
		return release;
	}

	public void setTrackNumber(short trackNumber) {
		this.trackNumber = trackNumber;
	}

	public void setTitle(String name) {
		setTitle(name, true);
	}

	public void setTitle(String name, boolean shorten) {
		this.title = shorten ? StringUtils.left(name, 512) : name;
	}

	public void setPosition(String position) {
		this.position = StringUtils.left(position, 255);
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setExtraArtists(Collection<ExtraArtist> extraArtists) {
		this.extraArtists = new HashSet<>(extraArtists);
	}

	public void setArtists(Collection<Artist> artists) {
		this.artists = new HashSet<>(artists);
	}

	public void setSequence(short sequence) {
		this.id.setSequenceId(sequence);
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
	public boolean isApplicable(Set<String> applicableTracks) {
		if (applicableTracks == null || applicableTracks.isEmpty()) {
			return true;
		}
		
		if (this.position == null) {
			return false;
		}
		
		if (Strings.CS.containsAny(this.position, applicableTracks.toArray(new String[applicableTracks.size()]))) {	// Obvious
			return true;
		}
		
		boolean applicable = false;
		Iterator<String> it = applicableTracks.iterator();
		
		while (it.hasNext() && !applicable) {
			String[] range = it.next().split("\\s[Tt]o\\s*");	// e.g. "A1 to A3" case insensitive
			
			switch (range.length) {
			case 1:
				// Useful for position = 1.02 and applicable = 1.2
				applicable = isApplicable(range[0], range[0]);
				break;
			case 2:
				applicable = isApplicable(range[0], range[1]);
				break;
			default:
				break;
			}
		}
		
		return applicable;
	}

	private boolean isApplicable(String lowerBound, String upperBound) {
		try {
			int ip = Integer.parseInt(this.position);
			
			return Integer.parseInt(lowerBound) <= ip && Integer.parseInt(upperBound) >= ip;
		} catch (NumberFormatException e) {
			String pf = format(this.position);
			
			return format(lowerBound).compareTo(pf) <= 0 && format(upperBound).compareTo(pf) >= 0; 
		}
	}
	
	private static String format(String input) {
		StringBuilder sb = new StringBuilder();
		int offset = 0;
		
		Matcher m = P.matcher(input);
		
		while (m.find()) {
			sb.append(input.substring(offset, m.start()));
			
			try {
				sb.append(String.format("%010d", Integer.parseInt(m.group())));	// Integer.MAX_VALUE = 2.147.483.648 (10 digits)
			} catch (NumberFormatException e) {
				// Happens if number exeeds Integer.MAX_VALUE. Simply append the original number.
				// Using Long instead of Integer solves only a few extraordinary situations
				// but blows up the formatted value
				sb.append(m.group());
			}
			
			offset = m.end();
		}
		
		sb.append(input.substring(offset));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "Track [id=" + id + ", trackNumber=" + trackNumber + ", position=" + position + ", title="
				+ title + ", duration=" + duration + ", artists=" + artists + ", extraArtists=" + extraArtists
				+ ", subTracklist=" + subTracklist + "]";
	}
}