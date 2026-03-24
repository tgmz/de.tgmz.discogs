/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
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
 * Role entity. It describes the kind of contribution of an artist in a release/track/subtrack e.g. "Written-By".
 */
@Entity
public class Role extends AtomicEntity<String> {
	@Transient
	private static final long serialVersionUID = -5492233327108668138L;

	public Role() {
		super();
	}

	public Role(String id) {
		super(id);
	}
}