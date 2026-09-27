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
public final class ExtraArtistKey implements Serializable {
	@Transient
	private static final long serialVersionUID = 5444222193660844182L;
	
	@Column(name = "credit_id")
	private String creditId;
	@Column(name = "detail_id")
	private String detailId;
	@Column(name = "artist_id")
	private int artistId;
	
	public ExtraArtistKey() {
		this(null, null, 0);
	}
	
	public ExtraArtistKey(String creditId, String detailId, int artistId) {
		super();
		setCreditId(creditId);
		setDetailId(detailId);
		setArtistId(artistId);
	}

	public String getCreditId() {
		return creditId;
	}
	public String getDetailId() {
		return detailId;
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
		return Objects.hash(Integer.valueOf(artistId), detailId, creditId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtraArtistKey other = (ExtraArtistKey) obj;
		return artistId == other.artistId && Objects.equals(detailId, other.detailId)
				&& Objects.equals(creditId, other.creditId);
	}

	@Override
	public String toString() {
		return "ExtraArtistKey [creditId=" + creditId + ", detailId=" + detailId + ", artistId=" + artistId + "]";
	}
}
