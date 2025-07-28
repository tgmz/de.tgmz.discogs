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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(indexes = {
	@Index(name = "companyRole_idx", columnList = "company, role")
})
@NamedQuery(name = "CompanyRole.byCompanyIdAndRole"
	, query = "FROM CompanyRole WHERE company.id = ?1 AND role = ?2") 
public class CompanyRole implements Serializable { 
	@Transient
	private static final long serialVersionUID = 6542992827176172533L;
	@Id
	@GeneratedValue
	private long id;
	private String role;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Company company;

	public CompanyRole() {
		company = new Company();
	}
	
	public CompanyRole(Company company, String role) {
		this.company = company;
		this.role = role;
	}

	public Company getCompany() {
		return company;
	}

	public String getRole() {
		return role;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	@Override
	public String toString() {
		return "CompanyRole [role=" + role + ", company=" + company + "]";
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
		CompanyRole other = (CompanyRole) obj;
		return this.company.getId() == other.company.getId() && Objects.equals(role, other.role);
	}
}
