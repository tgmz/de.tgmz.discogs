/*********************************************************************
* Copyright (c) 06.02.2025 Thomas Zierer
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

import de.tgmz.discogs.domain.id.CompanyRoleId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(indexes = {
	@Index(name = "companyRole_idx", columnList = "company, role")
})
public class CompanyRole implements Serializable { 
	@Transient
	private static final long serialVersionUID = 6542992827176172533L;
	@EmbeddedId
	private CompanyRoleId id;

	public CompanyRole() {
		id = new CompanyRoleId();
	}
	
	public CompanyRole(Company company, String role) {
		this();
		
		this.id.setCompany(company);
		this.id.setRole(role);
	}

	public CompanyRoleId getId() {
		return id;
	}

	public void setId(CompanyRoleId id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompanyRole other = (CompanyRole) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "CompanyRole [id=" + id + "]";
	}
}
