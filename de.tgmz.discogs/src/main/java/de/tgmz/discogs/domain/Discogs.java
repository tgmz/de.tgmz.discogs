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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class Discogs implements Serializable {
	private static final long serialVersionUID = -8920772069254927533L;
	private String dataQuality;
	private String title;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Artist> artists;
	@Column(length = 512)
	private String displayArtist;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<GenreSpec> genreSpecs;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Style> styles;
	@Transient
	private List<Long> artistIds;
	@Transient
	private List<String> genreNames;
	@Transient
	private List<String> styleNames;

	protected Discogs() {
		artistIds = new LinkedList<>();
		genreNames = new LinkedList<>();
		styleNames = new LinkedList<>();
	}

	/**
	 * The masters title obtained from discogs &lt;title&gt; tag.
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * The artists involved.
	 * @return the artists
	 */
	public Set<Artist> getArtists() {
		return artists;
	}

	/**
	 * The artist(s) as displayed on the cover. Important for collaborations e.g. &apos;Prince And The New Power Generation&apos;
	 * @return
	 */
	public String getDisplayArtist() {
		return displayArtist;
	}

	public Set<GenreSpec> getGenres() {
		return genreSpecs;
	}

	public Set<Style> getStyles() {
		return styles;
	}

	public void setGenres(Set<GenreSpec> genreSpecs) {
		this.genreSpecs = genreSpecs;
	}

	public void setStyles(Set<Style> styles) {
		this.styles = styles;
	}

	public String getDataQuality() {
		return dataQuality;
	}

	public void setDataQuality(String dataQuality) {
		this.dataQuality = dataQuality;
	}

	/**
	 * The ids of the artists involved. Useful in ReleaseContentHandler if we decide not to persist a release.
	 * @return the artistIds
	 */
	public List<Long> getArtistIds() {
		return artistIds;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setArtists(Set<Artist> artists) {
		this.artists = artists;
	}

	public void setDisplayArtist(String displayArtist) {
		this.displayArtist = displayArtist;
	}

	public void setArtistIds(List<Long> artistIds) {
		this.artistIds = artistIds;
	}

	public List<String> getGenreNames() {
		return genreNames;
	}

	public List<String> getStyleNames() {
		return styleNames;
	}

	public void setGenreNames(List<String> genreNames) {
		this.genreNames = genreNames;
	}

	public void setStyleNames(List<String> styleNames) {
		this.styleNames = styleNames;
	}

	@Override
	public String toString() {
		return "[title=" + title + ", displayArtist=" + displayArtist + ", artists=" + artists + "]";
	}
}