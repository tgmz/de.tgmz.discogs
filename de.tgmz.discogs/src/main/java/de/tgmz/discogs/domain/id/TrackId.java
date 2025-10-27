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

import de.tgmz.discogs.domain.Release;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

public class TrackId implements Serializable {
	@Transient
	private static final long serialVersionUID = 8460138400268050387L;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Release release;
	private short sequence;
	
	public TrackId() {
		super();
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release release) {
		this.release = release;
	}

	public short getSequence() {
		return sequence;
	}

	public void setSequence(short sequence) {
		this.sequence = sequence;
	}

	@Override
	public int hashCode() {
		return Objects.hash(release.getId(), sequence);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrackId other = (TrackId) obj;
		return release.getId() == other.release.getId() && sequence == other.sequence;
	}

	@Override
	public String toString() {
		return "TrackId [release=" + release.getId() + ", sequence=" + sequence + "]";
	}
}
