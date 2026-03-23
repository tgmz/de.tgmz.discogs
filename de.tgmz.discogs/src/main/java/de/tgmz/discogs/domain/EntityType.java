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

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * The entity describes the companys kind of contribution to a release e.g. "Distributed By".   
 */
@Entity
public class EntityType implements Serializable {
	@Transient
	private static final long serialVersionUID = -4122244830701398362L;

	@Id
	private byte id;
	private String name;
	
	public byte getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setId(byte id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "EntityType [id=" + id + ", name=" + name + "]";
	}
}
