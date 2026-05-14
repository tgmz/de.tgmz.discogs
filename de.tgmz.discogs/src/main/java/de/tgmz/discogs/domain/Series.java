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
import jakarta.persistence.Transient;

/**
 * Series entity.
 */
@Entity
public class Series extends AtomicEntity<Integer> {
	@Transient
	private static final long serialVersionUID = -9147451633557425171L;
	private String name;
	private String catno;

	public Series() {
		super();
	}
	
	public Series(int id, String catno, String name) {
		super(id);

		this.catno = catno;
		this.name = name;
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
		return "Series [id=" + String.format("%,d", getId()) + ", catno=" + catno + ", name=" + name + "]";
	}
}