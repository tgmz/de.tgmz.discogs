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
import java.util.Objects;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * Genre entity
 */
@MappedSuperclass
public abstract class Nature implements Serializable {
	@Transient
	private static final long serialVersionUID = 5684918391708831387L;

	public abstract String getId();

	@Override
	public String toString() {
		return this.getClass().getTypeName() +  " [id=" + getId() + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nature other = (Nature) obj;
		return Objects.equals(getId(), other.getId());
	}
}