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

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * Common stuff of genre and style
 */
@MappedSuperclass
public abstract class AbstractGenre implements Serializable {
	@Transient
	private static final long serialVersionUID = 847950387443312464L;
	private String name;

	protected AbstractGenre() {
	}

	protected AbstractGenre(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}