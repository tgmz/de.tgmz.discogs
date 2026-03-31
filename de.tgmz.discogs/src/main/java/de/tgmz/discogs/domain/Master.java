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

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Master entity.
 */
@Entity
@Table(indexes = {
		@Index(columnList = "title", name = "title_idx"), 
		@Index(columnList = "albumArtist", name = "albumArtist_idx"),
	})
public class Master extends Discogs {
	@Transient
	private static final long serialVersionUID = -5230886354906404806L;
	private Integer published;

	/**
	 * The year the master was published. Obtained from discogs &lt;year&gt; tag. Renamed from year because
	 * year is a reseved word in most databases.
	 * @return the year
	 */
	public Integer getPublished() {
		return published;
	}

	public void setPublished(Integer published) {
		this.published = published;
	}

	@Override
	public String toString() {
		return "Master [id=" + String.format("%,d", getId()) + ", Discogs=" + super.toString() + "]";
	}
}