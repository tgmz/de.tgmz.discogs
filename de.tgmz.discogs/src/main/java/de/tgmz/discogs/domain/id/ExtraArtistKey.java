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
	
	@Column(name = "role_id")
	private String roleId;
	@Column(name = "detail_id")
	private String detailId;
	@Column(name = "artist_id")
	private int artistId;
	
	public ExtraArtistKey() {
		this(null, null, 0);
	}
	
	public ExtraArtistKey(String roleId, String detailId, int artistId) {
		super();
		setRoleId(roleId);
		setDetailId(detailId);
		setArtistId(artistId);
	}

	public String getRoleId() {
		return roleId;
	}
	public String getDetailId() {
		return detailId;
	}
	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public void setDetailId(String detailId) {
		this.detailId = detailId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(artistId), detailId, roleId);
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
				&& Objects.equals(roleId, other.roleId);
	}

	@Override
	public String toString() {
		return "ExtraArtistKey [roleId=" + roleId + ", detailId=" + detailId + ", artistId=" + artistId + "]";
	}
}
