/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Artist entity.
 */
@Entity
@Table(indexes = {
	@Index(columnList = "name", name = "name_idx"),
})
public class Artist implements Serializable {
	@Transient
	private static final long serialVersionUID = -5230886354906404806L;
	@Id
	private long id;
	private String name;
	private String realName;
	@ElementCollection
	private Set<String> variations;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "artist_members")
	private List<Artist> members;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "artist_aliases")
	private List<Artist> aliases;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "artist_groups")
	private List<Artist> groups;
	@Enumerated(EnumType.ORDINAL)
	private DataQuality dataQuality;

	public Artist() {
		variations = new TreeSet<>();
		aliases = new LinkedList<>();
		groups = new LinkedList<>();
		members = new LinkedList<>();
	}
	/**
	 * The artists id obtained from discogs <id> tag.
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * The artists name obtained from discogs <name> tag.
	 * @return the id
	 */
	public String getName() {
		return name;
	}

	public String getRealName() {
		return realName;
	}
	
	/**
	 * Variation of the artists name. Useful for finding typos etc.
	 * @return the id
	 */
	public Set<String> getVariations() {
		return variations;
	}

	public List<Artist> getMembers() {
		return members;
	}

	public DataQuality getDataQuality() {
		return dataQuality;
	}
	
	public List<Artist> getAliases() {
		return aliases;
	}
	
	public List<Artist> getGroups() {
		return groups;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setDataQuality(DataQuality dataQuality) {
		this.dataQuality = dataQuality;
	}
	
	public void setVariations(Set<String> variations) {
		this.variations = variations;
	}
	
	public void setMembers(List<Artist> members) {
		this.members = members;
	}
	
	public void setAliases(List<Artist> aliases) {
		this.aliases = aliases;
	}

	public void setGroups(List<Artist> groups) {
		this.groups = groups;
	}
	
	@Override
	public String toString() {
		return "Artist [id=" + String.format("%,d", id) + ", name=" + name + "]";
	}
}