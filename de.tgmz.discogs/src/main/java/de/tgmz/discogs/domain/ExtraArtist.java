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
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(indexes = {
	@Index(name = "artistRole_idx", columnList = "artist, role")
})
public class ExtraArtist implements Serializable { 
	@Transient
	private static final long serialVersionUID = 2296552658329482485L;
	@EmbeddedId
	private ExtraArtistId id;
	public ExtraArtist() {
		id = new ExtraArtistId();
		
		id.setArtist(new Artist());
	}
	
	public ExtraArtist(Artist artist, String role) {
		this();
		
		this.id.setArtist(artist);
		this.id.setRole(role);
	}

	public ExtraArtistId getId() {
		return id;
	}
	
	public Artist getArtist() {
		return id.getArtist();
	}

	public String getRole() {
		return id.getRole();
	}

	public void setArtist(Artist a) {
		id.setArtist(a);
	}
	
	public void setRole(String a) {
		id.setRole(a);
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
}
