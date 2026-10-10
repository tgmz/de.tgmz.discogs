/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tgmz.discogs.domain.id.ReleaseExtraArtistKey;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(indexes = {
	@Index(columnList = "release_id,credit_id,detail_id,artist_id", name = "ReleaseExtraArtist_pkey", unique = true),
})
public class ReleaseExtraArtist implements IIdentifiable<ReleaseExtraArtistKey> {
	@Transient
	private static final long serialVersionUID = 1087312827584224994L;
	
	@Transient
	private static final Pattern P = Pattern.compile("(\\d+)");
	
	@EmbeddedId
	private ReleaseExtraArtistKey id;
	
	@ManyToOne
	@MapsId("releaseId")
	@JoinColumn(name = "release_id")
	private Release release;
	
	@ManyToOne
	@MapsId("extraArtistId")
	@JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
	@JoinColumn(name = "detail_id", referencedColumnName = "detail_id")
	@JoinColumn(name = "artist_id", referencedColumnName = "artist_id")
	private ExtraArtist extraArtist;
	
	@ElementCollection
	private Set<String> applicableTracks;
	
	public ReleaseExtraArtist() {
		id = new ReleaseExtraArtistKey();
		
		applicableTracks = new HashSet<>();
	}
	
	@Override
	public ReleaseExtraArtistKey getId() {
		return id;
	}
	
	public Release getRelease() {
		return release;
	}
	public ExtraArtist getExtraArtist() {
		return extraArtist;
	}
	public Set<String> getApplicableTracks() {
		return applicableTracks;
	}
	public void setRelease(Release release) {
		this.release = release;
		this.id.setReleaseId(release.getId());
	}
	public void setApplicableTracks(Set<String> applicableTracks) {
		this.applicableTracks = applicableTracks;
	}
	public void setExtraArtist(ExtraArtist extraArtist) {
		this.extraArtist = extraArtist;
		this.id.setArtistId(extraArtist.getArtist().getId());
		this.id.setCreditId(extraArtist.getCredit());
		this.id.setDetailId(extraArtist.getDetail());
	}

	/**
	 * Computes if the ExatraArtist applies to track/subtrack position.
	 * @param ea the ExatraArtist
	 */
	public boolean isApplicable(String position) {
		if (applicableTracks.isEmpty()) {
			return true;
		}
		
		if (position == null) {
			return false;
		}

		if (applicableTracks.contains(position)) {	// Obvious
			return true;
		}
		
		boolean applicable = false;
		Iterator<String> it = applicableTracks.iterator();
		
		while (it.hasNext() && !applicable) {
			String[] range = it.next().split("\\s[Tt]o\\s*");	// e.g. "A1 to A3" case insensitive
			
			switch (range.length) {
			case 1:
				// Useful for position = 1.02 and applicable = 1.2
				applicable = isApplicable(range[0], range[0], position);
				break;
			case 2:
				applicable = isApplicable(range[0], range[1], position);
				break;
			default:
				break;
			}
		}
		
		return applicable;
	}

	private boolean isApplicable(String lowerBound, String upperBound, String position) {
		try {
			int ip = Integer.parseInt(position);
			
			return Integer.parseInt(lowerBound) <= ip && Integer.parseInt(upperBound) >= ip;
		} catch (NumberFormatException e) {
			String pf = format(position);
			
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
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReleaseExtraArtist))
			return false;
		ReleaseExtraArtist other = (ReleaseExtraArtist) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "ReleaseExtraArtist [id=" + id + ", applicableTracks=" + applicableTracks + "]";
	}
}