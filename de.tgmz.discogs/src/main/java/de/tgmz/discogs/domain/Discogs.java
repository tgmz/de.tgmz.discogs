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
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Discogs implements Serializable {
	private static final long serialVersionUID = -8920772069254927533L;
	@Enumerated(EnumType.STRING)
	private DataQuality dataQuality;
	private String title;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private List<Artist> artists;
	@Column(length = 512)
	private String displayArtist;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private List<Genre> genres;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private List<Style> styles;

	protected Discogs() {
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
	public List<Artist> getArtists() {
		return artists;
	}

	/**
	 * The artist(s) as displayed on the cover. Important for collaborations e.g. &apos;Prince And The New Power Generation&apos;
	 * @return the artist(s) as displayed on the cover
	 */
	public String getDisplayArtist() {
		return displayArtist;
	}

	/**
	 * The genres e.g. &apos;Electronic&apos;
	 * @return the genres
	 */
	public List<Genre> getGenres() {
		return genres;
	}

	/**
	 * The styles e.g. &apos;Synth-pop&apos;
	 * @return the styles
	 */
	public List<Style> getStyles() {
		return styles;
	}

	/**
	 * The data quality e.g. &apos;Correct&apos;, &apos;Needs vote&apos
	 * @return the styles
	 */
	public DataQuality getDataQuality() {
		return dataQuality;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

	public void setDisplayArtist(String displayArtist) {
		this.displayArtist = displayArtist;
	}

	public void setGenres(List<Genre> genres) {
		this.genres = genres;
	}

	public void setStyles(List<Style> styles) {
		this.styles = styles;
	}

	public void setDataQuality(DataQuality dataQuality) {
		this.dataQuality = dataQuality;
	}

	@Override
	public String toString() {
		return "[title=" + title + ", displayArtist=" + displayArtist + ", artists=" + artists + "]";
	}
}