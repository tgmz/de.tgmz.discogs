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
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * Series entity.
 */
@Entity
public class Series implements Serializable {
	@Transient
	private static final long serialVersionUID = -9147451633557425171L;
	@Id
	private long id;
	private String name;
	private String catno;

	public Series() {
		super();
	}
	
	public Series(long id, String catno, String name) {
		this();
		this.id = id;
		this.catno = catno;
		this.name = name;
	}

	/**
	 * The series id obtained from discogs <id> tag.
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * The series name obtained from discogs <name> tag.
	 * @return the id
	 */
	public String getName() {
		return name;
	}

	public String getCatno() {
		return catno;
	}

	@Override
	public String toString() {
		return "Series [id=" + String.format("%,d", id) + ", catno=" + catno + ", name=" + name + "]";
	}
}