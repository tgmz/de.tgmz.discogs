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

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Master entity.
 */
@Entity
@Table(indexes = {
		@Index(columnList = "title", name = "Master_title_idx"), 
		@Index(columnList = "albumArtist", name = "Master_albumArtist_idx"),
	})
public class Master extends Discogs {
	@Transient
	private static final long serialVersionUID = -5230886354906404806L;
	private Integer published;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Video> videos;

	public Master() {
		super();
		
		videos = new HashSet<>();
	}
	/**
	 * The year the master was published. Obtained from discogs &lt;year&gt; tag. Renamed from year because
	 * year is a reseved word in most databases.
	 * @return the year
	 */
	public Integer getPublished() {
		return published;
	}

	public Set<Video> getVideos() {
		return videos;
	}

	public void setPublished(Integer published) {
		this.published = published;
	}

	public void setVideos(Set<Video> videos) {
		this.videos = videos;
	}
	
	@Override
	public String toString() {
		return "Master [id=" + String.format("%,d", getId()) + ", Discogs=" + super.toString() + "]";
	}
}