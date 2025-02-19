/*********************************************************************
* Copyright (c) 19.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.io.Serializable;

import jakarta.persistence.Entity;
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
public class Label implements Serializable {
	@Transient
	private static final long serialVersionUID = 1827497522531949133L;
	@Id
	private long id;
	private String name;
	private String dataQuality;

	/**
	 * The artists id obtained from discogs <id> tag.
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * The artists name obtained from discogs <name> tag.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public String getDataQuality() {
		return dataQuality;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDataQuality(String dataQuality) {
		this.dataQuality = dataQuality;
	}

	@Override
	public String toString() {
		return "Label [id=" + String.format("%,d", id) + ", name=" + name + "]";
	}
}