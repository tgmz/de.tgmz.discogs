/*********************************************************************
* Copyright (c) 08.12.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

@Entity
public class Identifier implements Serializable { 
	@Transient
	private static final long serialVersionUID = 46435372147406742L;
	private static final int MAX_LENGTH = 255;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "identifier_gen")
	@TableGenerator(name = "identifier_gen", allocationSize = 1, initialValue = 1)
	private int id;
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "_type", columnDefinition = "smallint")
	private IdentType type;
	@Column(name = "_description")
	private String description;
	@Column(name = "_value")
	private String value;
	
	public Identifier() {
		super();
	}

	public Identifier(IdentType type, String description, String value) {
		this();
		
		this.type = type;
		this.description = StringUtils.left(description, MAX_LENGTH);
		this.value = StringUtils.left(value, MAX_LENGTH);
	}

	public IdentType getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Identifier [id=" + id + ", type=" + type + ", description=" + description + ", value=" + value + "]";
	}
}
