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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Artist entity.
 */
@Entity
@Table(indexes = {
	@Index(columnList = "name", name = "Artist_name_idx"),
})
@NamedQuery(name = "Artist.byName"
, query = "FROM Artist a WHERE a.name = ?1") 
public class Artist extends PrimaryEntity {
	@Transient
	private static final long serialVersionUID = -5230886354906404806L;
	private String name;
	private String realname;
	@ElementCollection
	@JoinTable(indexes = {
		@Index(name = "Artist_variations_name_idx", columnList = "variations")	
	})
	private Set<String> variations;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "artist_members")
	private Set<Artist> members;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "artist_aliases")
	private Set<Artist> aliases;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(name = "artist_groups")
	private Set<Artist> groups;

	public Artist() {
		this(0);
	}

	public Artist(int id) {
		super(id);
		
		variations = new TreeSet<>();
		aliases = new HashSet<>();
		groups = new HashSet<>();
		members = new HashSet<>();
	}

	/**
	 * The artists name obtained from discogs <name> tag.
	 * @return the id
	 */
	public String getName() {
		return name;
	}

	public String getRealname() {
		return realname;
	}
	
	/**
	 * Variation of the artists name. Useful for finding typos etc.
	 * @return the id
	 */
	public Set<String> getVariations() {
		return variations;
	}

	public Collection<Artist> getMembers() {
		return members;
	}

	public Collection<Artist> getAliases() {
		return aliases;
	}
	
	public Collection<Artist> getGroups() {
		return groups;
	}
	
	public void setName(String name) {
		this.name = StringUtils.left(name, 255);
	}

	public void setRealname(String realName) {
		this.realname = StringUtils.left(realName, 255);
	}

	public void setVariations(Set<String> variations) {
		this.variations = variations;
	}
	
	public void setMembers(Collection<Artist> members) {
		this.members = new HashSet<>(members);
	}
	
	public void setAliases(Collection<Artist> aliases) {
		this.aliases = new HashSet<>(aliases);
	}

	public void setGroups(Collection<Artist> groups) {
		this.groups = new HashSet<>(groups);
	}
	
	@Override
	public String toString() {
		return "Artist [id=" + String.format("%,d", getId()) + ", name=" + name + "]";
	}
}