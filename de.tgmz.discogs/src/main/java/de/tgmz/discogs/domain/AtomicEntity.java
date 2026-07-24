/*********************************************************************
* Copyright (c) 24.03.2026 Thomas Zierer
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

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * An entity which is definied by its id element i.e. two instances are equal iff their ids are equal.
 *  
 * @param <T>: the ids type
 */
@MappedSuperclass
public abstract class AtomicEntity<T extends Serializable> implements IIdentifiable<T> {
	@Transient
	private static final long serialVersionUID = -8652708757318456206L;
	@Id
	private T id;

	protected AtomicEntity() {
		super();
	}

	protected AtomicEntity(T id) {
		this();
		this.id = id;
	}

	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AtomicEntity))
			return false;
		AtomicEntity<?> other = (AtomicEntity<?>) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() +  " [id=" + id + "]";
	}
}
