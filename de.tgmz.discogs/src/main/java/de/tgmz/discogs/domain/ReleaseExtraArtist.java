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

import java.io.Serializable;
import java.util.Objects;

import de.tgmz.discogs.domain.id.ReleaseExtraArtistId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

@Entity
public class ReleaseExtraArtist implements Serializable {
	private static final long serialVersionUID = 1087312827584224994L;
	
	@EmbeddedId
	private ReleaseExtraArtistId id;
	
	@ManyToOne
	@MapsId("releaseId")
	@JoinColumn(name = "release_id")
	private Release release;
	
	@ManyToOne
	@MapsId("artistId")
	@JoinColumn(name = "artist_id")
	private Artist artist;
	
	@ManyToOne
	@MapsId("roleId")
	@JoinColumn(name = "role_id")
	private Role role;
	
	private String applicableTracks;
	
	public ReleaseExtraArtist() {
		id = new ReleaseExtraArtistId();
	}
	
	public Release getRelease() {
		return release;
	}
	public Artist getArtist() {
		return artist;
	}
	public Role getRole() {
		return role;
	}
	public String getApplicableTracks() {
		return applicableTracks;
	}
	public void setRelease(Release release) {
		this.release = release;
		this.id.setReleaseId(release.getId());
	}
	public void setArtist(Artist artist) {
		this.artist = artist;
		this.id.setArtistId(artist.getId());
	}
	public void setRole(Role role) {
		this.role = role;
		this.id.setRoleId(role.getId());
	}
	public void setApplicableTracks(String applicableTracks) {
		this.applicableTracks = applicableTracks;
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
		return "ReleaseExtraArtist [release=" + release + ", artist=" + artist + ", role=" + role
				+ ", applicableTracks=" + applicableTracks + "]";
	}
}