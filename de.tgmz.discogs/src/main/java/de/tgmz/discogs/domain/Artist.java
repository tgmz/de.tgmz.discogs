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
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Artist entity.
 */
@Entity
@Table(indexes = {
	@Index(columnList = "name", name = "name_idx"),
})
public class Artist implements Serializable {
	@Transient
	private static final long serialVersionUID = -5230886354906404806L;
	@Id
	private long id;
	private String name;
	@ElementCollection
	private Set<String> variations;
	@Enumerated(EnumType.ORDINAL)
	private DataQuality dataQuality;

	/**
	 * The artists id obtained from discogs <id> tag.
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * The artists name obtained from discogs <name> tag.
	 * @return the id
	 */
	public String getName() {
		return name;
	}

	/**
	 * Variation of the artists name. Useful for finding typos etc.
	 * @return the id
	 */
	public Set<String> getVariations() {
		return variations;
	}

	public DataQuality getDataQuality() {
		return dataQuality;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVariations(Set<String> variations) {
		this.variations = variations;
	}

	public void setDataQuality(DataQuality dataQuality) {
		this.dataQuality = dataQuality;
	}
	
	@Override
	public String toString() {
		return "Artist [id=" + String.format("%,d", id) + ", name=" + name + ", variations=" + variations + "]";
	}
}