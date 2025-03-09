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

	public ExtraArtist() {
	}
	
	public ExtraArtist(String role, Artist artist) {
		super();
		this.role = role;
		this.artist = artist;
	}
	/**
	 * The id (generated)
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * The role e.g. &apos;Engeneer&apos;
	 * @return the role
	 */
	public String getRole() {
		return role;
	}
	
	/**
	 * The artist
	 * @return the artist
	 */
	public Artist getArtist() {
		return artist;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public void setArtist(Artist extraArtists) {
		this.artist = extraArtists;
	}
	
	@Override
	public String toString() {
		return "ExtraArtist [id=" + id + ", role=" + role + ", artist=" + artist + "]";
	}
}
