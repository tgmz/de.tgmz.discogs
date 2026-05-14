/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain.id;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

@Embeddable
public final class ReleaseCompanyKey implements Serializable {
	@Transient
	private static final long serialVersionUID = 8460138400268050387L;

	@Column(name = "release_id")
	private int releaseId;
	@Column(name = "company_id")
	private int companyId;
	@Column(name = "entityType_id")
	private short entityTypeId;
	
	public void setReleaseId(int releaseId) {
		this.releaseId = releaseId;
	}
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}
	public void setEntityTypeId(short entityTypeId) {
		this.entityTypeId = entityTypeId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(companyId, entityTypeId, releaseId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReleaseCompanyKey other = (ReleaseCompanyKey) obj;
		return companyId == other.companyId && entityTypeId == other.entityTypeId && releaseId == other.releaseId;
	}
}
