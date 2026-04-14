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

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

@Embeddable
public final class ReleaseExtraArtistKey implements Serializable {
	@Transient
	private static final long serialVersionUID = 5444222193660844182L;
	
	@Column(name = "release_id")
	private long releaseId;
	@Column(name = "artist_id")
	private long artistId;
	@Column(name = "role_id")
	private String roleId;
	
	public ReleaseExtraArtistKey() {
		this(0L, 0L, null);
	}
	
	public ReleaseExtraArtistKey(long releaseId, long artistId, String roleId) {
		super();
		setReleaseId(releaseId);
		setArtistId(artistId);
		setRoleId(roleId);
	}

	public String getRoleId() {
		return roleId;
	}
	public void setReleaseId(long releaseId) {
		this.releaseId = releaseId;
	}
	public void setArtistId(long artistId) {
		this.artistId = artistId;
	}
	public void setRoleId(String roleId) {
		this.roleId = StringUtils.left(roleId, 255);
	}
	@Override
	public int hashCode() {
		return Objects.hash(artistId, releaseId, roleId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReleaseExtraArtistKey))
			return false;
		ReleaseExtraArtistKey other = (ReleaseExtraArtistKey) obj;
		return artistId == other.artistId && releaseId == other.releaseId && Objects.equals(roleId, other.roleId);
	}
}
