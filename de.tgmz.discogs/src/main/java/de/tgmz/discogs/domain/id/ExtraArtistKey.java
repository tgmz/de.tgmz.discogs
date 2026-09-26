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
	@Column(name = "artist_id")
	private int artistId;
	
	public ExtraArtistKey() {
		this(null, 0);
	}
	
	public ExtraArtistKey(String roleId, int artistId) {
		super();
		setRoleId(roleId);
		setArtistId(artistId);
	}

	public String getRoleId() {
		return roleId;
	}
	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(artistId, roleId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExtraArtistKey))
			return false;
		ExtraArtistKey other = (ExtraArtistKey) obj;
		return artistId == other.artistId && Objects.equals(roleId, other.roleId);
	}

	@Override
	public String toString() {
		return "ReleaseExtraArtistKey [artistId=" + artistId + ", roleId=" + roleId + "]";
	}
}
