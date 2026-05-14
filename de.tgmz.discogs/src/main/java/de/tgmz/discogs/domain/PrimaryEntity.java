/*********************************************************************
* Copyright (c) 31.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * An entity that coresponds to a data dump file e.g. labels_<>.gz 
 *  
 */
@MappedSuperclass
public abstract class PrimaryEntity extends AtomicEntity<Integer> {
	@Transient
	private static final long serialVersionUID = -2201861486429131211L;
	@Enumerated(EnumType.ORDINAL)
	private DataQuality data_quality;

	protected PrimaryEntity() {
		super();
	}

	protected PrimaryEntity(int id) {
		this();
		
		setId(id);
	}

	public DataQuality getDataQuality() {
		return data_quality;
	}

	public void setDataQuality(DataQuality dataQuality) {
		this.data_quality = dataQuality;
	}
}
