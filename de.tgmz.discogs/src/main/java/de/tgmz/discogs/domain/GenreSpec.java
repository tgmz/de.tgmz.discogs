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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Transient;

/**
 * GenreSpec entity
 */
@Entity
@NamedQuery(name="GenreSpec.getByName", query="FROM GenreSpec WHERE name = ?1")
public class GenreSpec implements Serializable {
	@Transient
	private static final long serialVersionUID = 5684918391708831387L;
	@Id
	@GeneratedValue
	private long id;
	private String name;

	public GenreSpec() {
	}

	public GenreSpec(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "GenreSpec [id=" + id + ", name=" + name + "]";
	}
}