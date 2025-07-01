/*********************************************************************
* Copyright (c) 06.02.2025 Thomas Zierer
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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(indexes = {
	@Index(name = "artistRole_idx", columnList = "artist, role")
})
@NamedQuery(name = "ExtraArtist.byArtistIdAndRole"
	, query = "FROM ExtraArtist WHERE artist.id = ?1 AND role = ?2") 
public class ExtraArtist implements Serializable { 
	@Transient
	private static final long serialVersionUID = 2296552658329482485L;
	@Id
	@GeneratedValue
	private long id;
	@Column(length = 512)
	private String role;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Artist artist;

	public ExtraArtist() {
		artist = new Artist();
	}
	
	public ExtraArtist(Artist artist, String role) {
		this.artist = artist;
		this.role = role;
	}

	/**
	 * The artist
	 * @return the artist
	 */
	public Artist getArtist() {
		return artist;
	}

	/**
	 * The role e.g. &apos;Engeneer&apos;
	 * @return the role
	 */
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
	public String toString() {
		return "ExtraArtist [role=" + role + ", artist=" + artist + "]";
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
		ExtraArtist other = (ExtraArtist) obj;
		return this.artist.getId() == other.artist.getId() && Objects.equals(role, other.role);
	}
}
