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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
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
	@ElementCollection
	private Set<String> tracks;

	public ExtraArtist() {
		tracks = new TreeSet<>();
	}
	
	public ExtraArtist(String role, Artist artist, Set<String> tracks) {
		this();
		this.role = role;
		this.artist = artist;
		this.tracks.addAll(tracks);
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

	public Set<String> getTracks() {
		return tracks;
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

	public void setTracks(Set<String> tracks) {
		this.tracks.clear();
		this.tracks.addAll(tracks);
	}
	
	public void setTracks(String... tracks) {
		this.tracks.clear();
		
		Arrays.stream(tracks).forEach(s -> this.tracks.add(s));
	}
	
	/**
	 * Computes if the ExatraArtist applies to a track.
	 * @param t the track
	 */
	public boolean isApplicable(Track t) {
		if (this.getTracks().isEmpty()) {	// The ExtraArtist applies to every track
			return true;
		}
		
		if (t.getPosition() == null) {		// The ExtraArtist applies to some tracks, but we cannot decide if it applies to this one
			return false;
		}
		
		if (this.getTracks().contains(t.getPosition())) {	// Obvious
			return true;
		}
		
		boolean applicable = false;
		Iterator<String> it = this.getTracks().iterator();
		
		while (it.hasNext() && !applicable) {
			String[] range = it.next().split("\\sto\\s*");	// e.g. "A1 to A3"
			
			if (range.length == 2) {
				applicable = range[0].compareTo(t.getPosition()) <= 0 && range[1].compareTo(t.getPosition()) >= 0;  
			}
		}
		
		return applicable;
	}
	
	@Override
	public String toString() {
		return "ExtraArtist [id=" + id + ", role=" + role + ", artist=" + artist + "]";
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
		return Objects.equals(role, other.role) && this.artist.getId() == other.getArtist().getId();
	}
}
