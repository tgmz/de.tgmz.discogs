/*********************************************************************
* Copyright (c) 22.03.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.tgmz.discogs.domain.id.ReleaseExtraArtistKey;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "release_extraartist"
	, indexes = {
		@Index(columnList = "release_id,artist_id,role_id", name = "ReleaseExtraArtist_pkey", unique = true),
})
public class ReleaseExtraArtist implements Serializable {
	private static final long serialVersionUID = 1087312827584224994L;
	
	@EmbeddedId
	private ReleaseExtraArtistKey id;
	
	@ManyToOne
	@MapsId("releaseId")
	@JoinColumn(name = "release_id")
	private Release release;
	
	@ManyToOne
	@MapsId("extraArtistId")
	@JoinColumn(name = "artist_id", referencedColumnName = "artist_id")
	@JoinColumn(name = "role_id", referencedColumnName = "role")
	private ExtraArtist extraArtist;
	
	@ElementCollection
	private Set<String> applicableTracks;
	
	public ReleaseExtraArtist() {
		id = new ReleaseExtraArtistKey();
		
		applicableTracks = new HashSet<>();
	}
	
	public Release getRelease() {
		return release;
	}
	public ExtraArtist getExtraArtist() {
		return extraArtist;
	}
	public Set<String> getApplicableTracks() {
		return applicableTracks;
	}
	public void setRelease(Release release) {
		this.release = release;
		this.id.setReleaseId(release.getId());
	}
	public void setExtraArtist(ExtraArtist extraArtist) {
		this.extraArtist = extraArtist;
		this.id.setArtistId(extraArtist.getArtist().getId());
		this.id.setRoleId(extraArtist.getRole());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReleaseExtraArtist))
			return false;
		ReleaseExtraArtist other = (ReleaseExtraArtist) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "ReleaseExtraArtist [id=" + id + ", applicableTracks=" + applicableTracks + "]";
	}
}