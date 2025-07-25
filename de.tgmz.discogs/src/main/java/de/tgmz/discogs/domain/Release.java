/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Release entity.
 */

@Entity
@Table(indexes = {
		@Index(columnList = "displayArtist,title", name = "displayArtist_title_idx"), 
		@Index(columnList = "displayArtist", name = "displayArtist_idx"), 
		@Index(columnList = "title", name = "title_idx"), 
	})
public class Release extends Discogs {
	@Transient
	private static final long serialVersionUID = -8124211768010344837L;
	@Id
	private long id;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy(value = "sequence")
	private List<Track> tracklist;
	private boolean _main;
	@ManyToOne
	private Master master;
	private String country;
	private String released;
	@ElementCollection(fetch = FetchType.LAZY)
	private Map<ExtraArtist, String> extraArtists;
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "catno")
	private Map<Label, String> labels;	

	public Release() {
		super();
		
		tracklist = new LinkedList<>();
		extraArtists = new HashMap<>();
		labels = new HashMap<>();
	}
	
	@Override
	public long getId() {
		return id;
	}

	/**
	 * The tracks.
	 * @return the tracks
	 */
	public List<Track> getUnfilteredTracklist() {
		return tracklist;
	}

	public List<Track> getTracklist() {
		return tracklist.stream().filter(t -> !((t.getPosition() == null && t.getDuration() == null) && t.getSubTracklist().isEmpty())).toList();
	}

	public boolean isMain() {
		return _main;
	}

	public String getCountry() {
		return country;
	}

	public String getReleased() {
		return released;
	}

	public Map<Label, String> getLabels() {
		return labels;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public Master getMaster() {
		return master;
	}

	public void setMain(boolean newMain) {
		this._main = newMain;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setReleased(String released) {
		this.released = released;
	}

	public void setMaster(Master master) {
		this.master = master;
	}

	public Map<ExtraArtist, String> getExtraArtists() {
		return extraArtists;
	}

	/**
	 * Setter for labels. We need a setter here because the SAX handler manipulates the keyset.
	 */
	public void setLabels(Map<Label, String> labels) {
		this.labels = labels;
	}

	/**
	 * Setter for extraArtists. We need a setter here because the SAX handler manipulates the keyset.
	 */
	public void setExtraArtists(Map<ExtraArtist, String> extraArtists) {
		this.extraArtists = extraArtists;
	}
	
	/**
	 * Compute the amount of information of this release
	 * @return A measure for the amount of information this release carries
	 */
	public int sizeOf() {
		int i = 0;
		
		for (Track t : tracklist) {
			i += t.sizeOf();
			
			i += extraArtists.entrySet().stream().filter(e -> t.isApplicable(e.getValue())).count();
		}
		
		return i;
	}
	
	@Override
	public String toString() {
		return "Release [id=" + String.format("%,d", id) + ", Discogs=" + super.toString() + "]";
	}
}