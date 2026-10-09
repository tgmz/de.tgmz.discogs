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
import java.util.function.Function;
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

	private static final Pattern P0 = Pattern.compile("(\\D+)(\\d+)");		// A7
	private static final Pattern P1 = Pattern.compile("(\\D+)\\s\\.-(\\d+)");		// CD2-4
	private static final Pattern P2 = Pattern.compile("(\\d+)[\\.-](\\d+)");	// 4-13, 1.02
	private static final Pattern P3 = Pattern.compile("(\\d+)(\\.)");			// 2.
	
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
				applicable = isInRange(range[0], range[0]);
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
			int il = Integer.parseInt(lowerBound);
			int iu = Integer.parseInt(upperBound);
			int ip = Integer.parseInt(this.position);
			
			return il <= ip && iu >= ip;
		} catch (NumberFormatException e) {
			return isInRange(lowerBound, upperBound); 
		}
	}

	private boolean isInRange(String lowerBound, String upperBound) {
		return isInRange(P0, lowerBound, upperBound, Function.identity(), Integer::parseInt) 
				|| isInRange(P1, lowerBound, upperBound, Function.identity(), Integer::parseInt) 
				|| isInRange(P2, lowerBound, upperBound, Integer::parseInt, Integer::parseInt) 
				|| isInRange(P3, lowerBound, upperBound, Integer::parseInt, Function.identity())
				|| lowerBound.compareTo(this.position) <= 0 && upperBound.compareTo(this.position) >= 0;	// Fallback 
	}
	
	/**
	 * Handle situations like lowerBound = "A6", upperBound = "A10", position = "A7"
	 * The default comparision yields "false" as "A7" is lexicographically larger than "A10"
	 * We split bounds and position into pre and post, so e.g. "A11" becomes String "A"
	 * and int 11. 
	 * 
	 * @param <T0> the type pre will be converted to  
	 * @param <T1> the type post will be converted to
	 * @param p the pattern to split pre and post
	 * @param lowerBound
	 * @param upperBound
	 * @param f0 converter function for pre e.g. Integer::parseInt
	 * @param f1 converter function for post
	 * @return
	 */
	private <T0 extends Comparable<T0>, T1 extends Comparable<T1>>
		boolean isInRange(Pattern p
				, String lowerBound, String upperBound
				, Function<String, T0> f0, Function<String, T1> f1) {
		Matcher ml = p.matcher(lowerBound);
		
		if (ml.matches() && ml.groupCount() == 2) {
			T0 lb0 = f0.apply(ml.group(1));				// lower bound pre
			T1 lb1 = f1.apply(ml.group(2));				// lower bound post
					
			Matcher mu = p.matcher(upperBound);
			
			if (mu.matches() && mu.groupCount() == 2) {
				T0 ub0 = f0.apply(mu.group(1));			// upper bound pre
				T1 ub1 = f1.apply(mu.group(2));			// upper bound post
						
				Matcher mp = p.matcher(this.position);
				
				if (mp.matches() && mp.groupCount() == 2) {
					T0 p0 = f0.apply(mp.group(1));		// position pre
					T1 p1 = f1.apply(mp.group(2));		// position post
					
					return lb0.compareTo(p0) <= 0 
						&& ub0.compareTo(p0) >= 0
						&& lb1.compareTo(p1) <= 0 
						&& ub1.compareTo(p1) >= 0;   
				}
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Track [id=" + id + ", trackNumber=" + trackNumber + ", position=" + position + ", title="
				+ title + ", duration=" + duration + ", artists=" + artists + ", extraArtists=" + extraArtists
				+ ", subTracklist=" + subTracklist + "]";
	}
}