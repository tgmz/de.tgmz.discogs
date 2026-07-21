/*********************************************************************
* Copyright (c) 19.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Label entity.
 */
@Entity
@Table(indexes = {
	@Index(columnList = "name", name = "Label_name_idx"),
})
public class Label extends PrimaryEntity {
	@Transient
	private static final long serialVersionUID = 1827497522531949133L;
	private String name;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Label parentLabel;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Label> subLabels;

	public Label() {
		super();
		
		subLabels = new HashSet<>();
	}
	/**
	 * The labels name obtained from discogs <name> tag.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public Label getParentLabel() {
		return parentLabel;
	}
	
	public Set<Label> getSubLabels() {
		return subLabels;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentLabel(Label parentLabel) {
		this.parentLabel = parentLabel;
	}

	public void setSubLabels(Set<Label> subLabels) {
		this.subLabels = subLabels;
	}

	@Override
	public String toString() {
		return "Label [id=" + String.format("%,d", getId()) + ", name=" + name + "]";
	}
}