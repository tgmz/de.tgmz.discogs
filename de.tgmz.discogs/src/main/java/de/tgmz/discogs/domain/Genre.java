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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Genre entity
 */
@Entity
@AttributeOverride(name = "id", column = @Column(length = 31))
public class Genre extends AtomicEntity<String> {
	@Transient
	private static final long serialVersionUID = 5684918391708831387L;

	public Genre() {
		super();
	}

	public Genre(String id) {
		super(id);
	}
}