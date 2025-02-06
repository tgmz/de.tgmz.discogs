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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ExtraArtist implements Serializable { 
	private static final long serialVersionUID = 2296552658329482485L;
	@Id
	@GeneratedValue
	private long id;
	private String role;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Artist artist;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Artist getArtist() {
		return artist;
	}
	public void setArtist(Artist extraArtists) {
		this.artist = extraArtists;
	}
	@Override
	public String toString() {
		return "ExtraArtist [id=" + id + ", role=" + role + ", artist=" + artist + "]";
	}
}
