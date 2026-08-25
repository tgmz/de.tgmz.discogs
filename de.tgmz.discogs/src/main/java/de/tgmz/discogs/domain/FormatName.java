/*********************************************************************
* Copyright (c) 20.08.2026 Thomas Zierer
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

@Entity
@AttributeOverride(name = "id", column = @Column(length = 31))
public class FormatName extends AtomicEntity<String> {
	@Transient
	private static final long serialVersionUID = -2954766668211762764L;

	public FormatName() {
		super();
	}

	public FormatName(String id) {
		super(id);
	}
}