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

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Embeddable
public class ExtraArtistId implements Serializable {
	@Transient
	private static final long serialVersionUID = 9128908612765815149L;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "artist_id")
	private Artist artist;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "role_id")
	private Role role;
	
	public ExtraArtistId() {
		this(new Artist(), new Role());
	}

	public ExtraArtistId(Artist artist, Role role) {
		super();
		
		this.artist = artist;
		this.role = role;
	}

	public Artist getArtist() {
		return artist;
	}

	public Role getRole() {
		return role;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist.getId(), role);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExtraArtistId))
			return false;
		ExtraArtistId other = (ExtraArtistId) obj;
		return Objects.equals(artist.getId(), other.artist.getId()) && Objects.equals(role, other.role);
	}

	@Override
	public String toString() {
		return "ExtraArtistId [role=" + role + ", artist=" + artist + "]";
	}

}
