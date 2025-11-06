/*********************************************************************
* Copyright (c) 25.10.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain.id;

import java.io.Serializable;
import java.util.Objects;

import de.tgmz.discogs.domain.Track;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

public class SubTrackId implements Serializable {
	@Transient
	private static final long serialVersionUID = 8460138400268050387L;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Track track;
	private short subTrackNumber;
	
	public SubTrackId() {
		super();
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}

	public short getSubTrackNumber() {
		return subTrackNumber;
	}

	public void setSubTrackNumber(short subTrackNumber) {
		this.subTrackNumber = subTrackNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(subTrackNumber, track.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubTrackId other = (SubTrackId) obj;
		return subTrackNumber == other.subTrackNumber && Objects.equals(track.getId(), other.track.getId());
	}

	@Override
	public String toString() {
		return "SubTrackId [track=" + track + ", sequence=" + subTrackNumber + "]";
	}
}
