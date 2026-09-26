/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

@Embeddable
public final class SubTrackKey implements Serializable {
	@Transient
	private static final long serialVersionUID = 6181832569215494280L;
	
	@Column(name = "release_id")
	private int releaseId;
	@Column(name = "sequence_id")
	private short sequenceId;
	@Column(name = "subtracknumber_id")
	private short subTrackNumberId;
	
	public int getReleaseId() {
		return releaseId;
	}
	public void setReleaseId(int releaseId) {
		this.releaseId = releaseId;
	}
	public short getSequenceId() {
		return sequenceId;
	}
	public void setSequenceId(short sequenceId) {
		this.sequenceId = sequenceId;
	}
	public short getSubTrackNumberId() {
		return subTrackNumberId;
	}
	public void setSubTrackNumberId(short subTrackNumberId) {
		this.subTrackNumberId = subTrackNumberId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(releaseId), Short.valueOf(sequenceId), Short.valueOf(subTrackNumberId));
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubTrackKey other = (SubTrackKey) obj;
		return releaseId == other.releaseId && sequenceId == other.sequenceId
				&& subTrackNumberId == other.subTrackNumberId;
	}
}
