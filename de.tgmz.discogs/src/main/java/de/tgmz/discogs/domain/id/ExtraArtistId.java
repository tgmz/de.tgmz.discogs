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
	private String role;
	
	public ExtraArtistId() {
		this(new Artist(), null);
	}

	public ExtraArtistId(Artist artist, String role) {
		super();
		
		setArtist(artist);
		setRole(role);
	}

	public Artist getArtist() {
		return artist;
	}

	public String getRole() {
		return role;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public void setRole(String role) {
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
