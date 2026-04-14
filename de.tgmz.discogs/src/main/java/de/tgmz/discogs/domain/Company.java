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
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Company entity.
 */
@Entity
@Table(indexes = {
	@Index(columnList = "name", name = "Company_name_idx"),
})
public class Company extends AtomicEntity<Long> {
	@Transient
	private static final long serialVersionUID = -2527932623058215441L;
	private String name;

	public Company() {
		super();
	}
	
	public Company(long id, String name) {
		super(id);
		
		this.name = name;
	}

	/**
	 * The companys name obtained from discogs <name> tag.
	 * @return the id
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Company [id=" + String.format("%,d", getId()) + ", name=" + name + "]";
	}
}