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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Identifier implements Serializable { 
	@Transient
	private static final long serialVersionUID = 46435372147406742L;
	private static final int MAX_LENGTH = 255;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	@Enumerated(EnumType.ORDINAL)
	private IdentType _type;
	private String _description;
	private String _value;
	
	public Identifier() {
		super();
	}

	public Identifier(IdentType type, String description, String value) {
		this();
		
		this._type = type;
		this._description = StringUtils.left(description, MAX_LENGTH);
		this._value = StringUtils.left(value, MAX_LENGTH);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public IdentType getType() {
		return _type;
	}

	public String getDescription() {
		return _description;
	}

	public String getValue() {
		return _value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_description, id, _type, _value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identifier other = (Identifier) obj;
		return Objects.equals(_description, other._description) && id == other.id && Objects.equals(_type, other._type)
				&& Objects.equals(_value, other._value);
	}

	@Override
	public String toString() {
		return "Identifier [id=" + id + ", type=" + _type + ", description=" + _description + ", value=" + _value + "]";
	}
}
