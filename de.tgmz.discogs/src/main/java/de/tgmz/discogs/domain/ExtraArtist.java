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

import de.tgmz.discogs.domain.id.ExtraArtistId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public class ExtraArtist implements Serializable { 
	@Transient
	private static final long serialVersionUID = 2296552658329482485L;
	@EmbeddedId
	private ExtraArtistId id;
	
	public ExtraArtist() {
		id = new ExtraArtistId();
	}

	public ExtraArtist(Artist artist, Role role) {
		this();
		
		this.id.setArtist(artist);
		this.id.setRole(role);
	}

	public ExtraArtistId getId() {
		return id;
	}

	public void setId(ExtraArtistId id) {
		this.id = id;
	}

	public Artist getArtist() {
		return id.getArtist();
	}

	public void setArtist(Artist artist) {
		this.id.setArtist(artist);
	}

	public Role getRole() {
		return id.getRole();
	}

	public void setRole(Role role) {
		this.id.setRole(role);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
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
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "ExtraArtist [id=" + id + "]";
	}
	
}
