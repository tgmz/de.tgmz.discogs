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
public final class ReleaseExtraArtistKey implements Serializable {
	@Transient
	private static final long serialVersionUID = 5444222193660844182L;
	
	@Column(name = "release_id")
	private int releaseId;
	@Column(name = "credit_id")
	private String creditId;
	@Column(name = "detail_id")
	private String detailId;
	@Column(name = "artist_id")
	private int artistId;
	
	public ReleaseExtraArtistKey() {
		this(0, null, null, 0);
	}
	
	public ReleaseExtraArtistKey(int releaseId, String creditId, String detailId, int artistId) {
		super();
		setReleaseId(releaseId);
		setCreditId(creditId);
		setDetailId(detailId);
		setArtistId(artistId);
	}

	public void setReleaseId(int releaseId) {
		this.releaseId = releaseId;
	}
	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}
	public void setCreditId(String creditId) {
		this.creditId = creditId;
	}
	public void setDetailId(String detailId) {
		this.detailId = detailId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(artistId), detailId, Integer.valueOf(releaseId), creditId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReleaseExtraArtistKey other = (ReleaseExtraArtistKey) obj;
		return artistId == other.artistId && Objects.equals(detailId, other.detailId) && releaseId == other.releaseId
				&& Objects.equals(creditId, other.creditId);
	}

	@Override
	public String toString() {
		return "ReleaseExtraArtistKey [releaseId=" + String.format("%,d", releaseId) + ", creditId=" + creditId + ", detailId=" + detailId
				+ ", artistId=" + String.format("%,d", artistId) + "]";
	}
}
