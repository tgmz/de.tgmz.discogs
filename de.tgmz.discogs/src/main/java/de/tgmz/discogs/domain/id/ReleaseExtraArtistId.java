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
public class ReleaseExtraArtistId implements Serializable {
	@Transient
	private static final long serialVersionUID = 5444222193660844182L;
	
	@Column(name = "release_id")
	private long releaseId;
	@Column(name = "artist_id")
	private long artistId;
	@Column(name = "role_id")
	private String roleId;
	
	public void setReleaseId(long releaseId) {
		this.releaseId = releaseId;
	}
	public void setArtistId(long artistId) {
		this.artistId = artistId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(artistId, roleId, releaseId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReleaseExtraArtistId other = (ReleaseExtraArtistId) obj;
		return artistId == other.artistId && roleId == other.roleId && releaseId == other.releaseId;
	}
}
