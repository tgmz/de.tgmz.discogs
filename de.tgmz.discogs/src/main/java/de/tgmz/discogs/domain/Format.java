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
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Format implements Serializable { 
	@Transient
	private static final long serialVersionUID = 1820280634515019733L;
	@Id
	@GeneratedValue
	private long id;
	private String name;
	private String qty;
	private String text;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> descriptions;
	
	public Format() {
		super();
		
		descriptions = new TreeSet<>();
	}

	public Format(String name, String qty, String text) {
		this();
		
		this.name = name;
		this.qty = qty;
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getQty() {
		return qty;
	}

	public String getText() {
		return text;
	}

	public Set<String> getDescriptions() {
		return descriptions;
	}

	@Override
	public int hashCode() {
		return Objects.hash(descriptions, id, name, qty, text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Format other = (Format) obj;
		return Objects.equals(descriptions, other.descriptions) && id == other.id && Objects.equals(name, other.name)
				&& qty == other.qty && Objects.equals(text, other.text);
	}

	@Override
	public String toString() {
		return "Format [id=" + id + ", name=" + name + ", qty=" + qty + ", text=" + text + ", descriptions="
				+ descriptions + "]";
	}
}
