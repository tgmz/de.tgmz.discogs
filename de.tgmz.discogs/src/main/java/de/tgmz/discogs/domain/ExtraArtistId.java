/*********************************************************************
* Copyright (c) 25.10.2025 Thomas Zierer
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

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

public class ExtraArtistId implements Serializable {
	@Transient
	private static final long serialVersionUID = 8460138400268050387L;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Artist artist;
	private String role;
	
	public ExtraArtistId() {
	}

	public ExtraArtistId(Artist artist, String role) {
		super();
		this.artist = artist;
		this.role = role;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, role);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtraArtistId other = (ExtraArtistId) obj;
		return artist.getId() == other.artist.getId() && Objects.equals(role, other.role);
	}

	@Override
	public String toString() {
		return "ExtraArtistId [artist=" + artist + ", role=" + role + "]";
	}
}
