/*********************************************************************
* Copyright (c) 06.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.Objects;

import de.tgmz.discogs.domain.id.ExtraArtistKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(indexes = {
	@Index(columnList = "credit_id,detail_id,artist_id", name = "ExtraArtist_pkey", unique = true),
})
public class ExtraArtist implements IIdentifiable<ExtraArtistKey> { 
	@Transient
	private static final long serialVersionUID = 2296552658329482485L;
	
	@EmbeddedId
	private ExtraArtistKey id;
	
	@ManyToOne
	@MapsId("artistId")
	@JoinColumn(name = "artist_id")
	private Artist artist;

	@Transient
	private String role;	// Use this field for temporarily storing the role before it is split into credit and detail
	
	public ExtraArtist() {
		id = new ExtraArtistKey();
		
		artist = new Artist();
	}

	public ExtraArtist(String credit, String detail, Artist artist) {
		this();
		
		this.id.setCreditId(credit);
		this.id.setDetailId(detail);
		this.setArtist(artist);
	}

	@Override
	public ExtraArtistKey getId() {
		return id;
	}

	public void setId(ExtraArtistKey id) {
		this.id = id;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
		this.id.setArtistId(artist.getId());
	}

	public String getCredit() {
		return id.getCreditId();
	}

	public String getDetail() {
		return id.getDetailId();
	}

	public String getRole() {
		return role;
	}

	public void setRole(String rrole) {
		this.role = rrole;
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
		ExtraArtist other = (ExtraArtist) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "ExtraArtist [id=" + id + "]";
	}

}
