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
import java.util.Objects;

import de.tgmz.discogs.domain.id.ReleaseCompanyId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

@Entity
public class ReleaseCompany implements Serializable {
	private static final long serialVersionUID = 1087312827584224994L;
	
	@EmbeddedId
	private ReleaseCompanyId id;
	
	@ManyToOne
	@MapsId("releaseId")
	@JoinColumn(name = "release_id")
	private Release release;
	
	@ManyToOne
	@MapsId("companyId")
	@JoinColumn(name = "company_id")
	private Company company;
	
	@ManyToOne
	@MapsId("entityTypeId")
	@JoinColumn(name = "entityType_id")
	private EntityType entityType;
	
	public ReleaseCompany() {
		id = new ReleaseCompanyId();
	}
	
	public Release getRelease() {
		return release;
	}
	public Company getCompany() {
		return company;
	}
	public EntityType getEntityType() {
		return entityType;
	}
	public void setRelease(Release release) {
		this.release = release;
		this.id.setReleaseId(release.getId());
	}
	public void setCompany(Company company) {
		this.company = company;
		this.id.setCompanyId(company.getId());
	}
	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
		this.id.setEntityTypeId(entityType.getId());
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
		ReleaseCompany other = (ReleaseCompany) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "ReleaseCompany [id=" + id + ", release=" + release + ", company=" + company + ", entityType="
				+ entityType + "]";
	}
}