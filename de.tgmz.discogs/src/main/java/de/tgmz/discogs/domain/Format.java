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
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

@Entity
//No need to reference IIdentifiable: An entitiy with a generated id cannot be used with a BasicEntityFactory 
public class Format implements Serializable {
	@Transient
	private static final long serialVersionUID = 1820280634515019733L;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "format_gen")
	@TableGenerator(name = "format_gen", allocationSize = 1, initialValue = 1)
	private int id;
	@ManyToOne
	@JoinColumn(name = "name")
	private FormatName name;
	private Float qty;
	private String text;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> descriptions;
	
	public Format() {
		super();
		
		descriptions = new TreeSet<>();
	}

	public Format(FormatName name, Float qty, String text) {
		this();
		
		this.name = name;
		this.qty = qty;
		this.text = StringUtils.left(text, 255);
	}

	public FormatName getName() {
		return name;
	}

	public void setName(FormatName name) {
		this.name = name;
	}

	public Float getQty() {
		return qty;
	}

	public String getText() {
		return text;
	}

	public Set<String> getDescriptions() {
		return descriptions;
	}

	@Override
	public String toString() {
		return "Format [id=" + id + ", name=" + name + ", qty=" + qty + ", text=" + text + ", descriptions="
				+ descriptions + "]";
	}
}
