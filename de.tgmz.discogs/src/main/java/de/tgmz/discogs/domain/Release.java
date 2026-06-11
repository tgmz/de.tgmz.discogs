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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
		@Index(columnList = "albumArtist", name = "Release_albumArtist_idx"), 
		@Index(columnList = "title", name = "Release_title_idx"), 
	})
public class Release extends Discogs {
	@Transient
	private static final long serialVersionUID = -8124211768010344837L;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy(value = "sequence")
	private List<Track> tracklist;
	private boolean _main;
	@ManyToOne
	private Master master;
	private String country;
	private String released;
	@OneToMany(mappedBy = "release", cascade = CascadeType.ALL)
	private Set<ReleaseExtraArtist> releaseExtraArtists;
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "catno")
	private Map<Label, String> labels;
	@OneToMany(mappedBy = "release", cascade = CascadeType.ALL)
	private Set<ReleaseCompany> releaseCompanies;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Format> formats;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Series series;

	public Release() {
		super();
		
		tracklist = new LinkedList<>();
		releaseExtraArtists = new HashSet<>();
		labels = new HashMap<>();
		releaseCompanies = new HashSet<>();
		formats = new HashSet<>();
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

	public Set<Format> getFormats() {
		return formats;
	}

	public Master getMaster() {
		return master;
	}

	public Set<ReleaseCompany> getReleaseCompanies() {
		return releaseCompanies;
	}

	public Series getSeries() {
		return series;
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

	public Set<ReleaseExtraArtist> getReleaseExtraArtists() {
		return releaseExtraArtists;
	}

	/**
	 * Setter for labels. We need a setter here because the SAX handler manipulates the keyset.
	 */
	public void setLabels(Map<Label, String> labels) {
		this.labels = labels;
	}

	public void setReleaseExtraArtists(Set<ReleaseExtraArtist> releaseExtraArtists) {
		this.releaseExtraArtists = releaseExtraArtists;
	}
	
	public void setSeries(Series series) {
		this.series = series;
	}

	public void setReleaseCompanies(Set<ReleaseCompany> releaseCompanies) {
		this.releaseCompanies = releaseCompanies;
	}
	
	/**
	 * Compute the amount of information of this release
	 * @return A measure for the amount of information this release carries
	 */
	public int sizeOf() {
		int i = 0;
		
		for (Track t : tracklist) {
			for (ReleaseExtraArtist rea : releaseExtraArtists) {
				i += t.isApplicable(rea.getApplicableTracks()) ? 1 : 0;
			}
			
			i += t.sizeOf();
		}
		
		return i;
	}
	
	@Override
	public String toString() {
		return "Release [id=" + String.format("%,d", getId()) + ", Discogs=" + super.toString() + "]";
	}
}