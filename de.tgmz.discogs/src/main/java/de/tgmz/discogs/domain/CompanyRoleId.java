/*********************************************************************
* Copyright (c) 25.10.2025 Thomas Zierer
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

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

public class CompanyRoleId implements Serializable {
	@Transient
	private static final long serialVersionUID = 8460138400268050387L;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Company company;
	private String role;
	
	public CompanyRoleId() {
		company = new Company();
	}

	public CompanyRoleId(Company company, String role) {
		this();
		
		this.company = company;
		this.role = role;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(company, role);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompanyRoleId other = (CompanyRoleId) obj;
		return company.getId() == other.company.getId() && Objects.equals(role, other.role);
	}

	@Override
	public String toString() {
		return "CompanyRoleId [company=" + company + ", role=" + role + "]";
	}
}
