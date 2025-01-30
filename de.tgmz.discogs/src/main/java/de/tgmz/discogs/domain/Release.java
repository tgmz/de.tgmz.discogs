package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Generated;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Release entity.
 */

@Entity
@Table(indexes = {
	@Index(columnList = "title", name = "title_idx"), 
	@Index(columnList = "displayArtist", name = "displayArtist_idx"),
})
public class Release implements IRelease {
	private static final long serialVersionUID = -8124211768010344837L;
	@Id
	private long id;
	private String title;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Artist> artists;
	@Column(length = 512)
	private String displayArtist;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@OrderBy(value = "position")
	private List<Track> tracklist;
	@Transient
	private List<Long> artistIds;

	public Release() {
		artistIds = new LinkedList<>();
	}

	/**
	 * The releases id obtained from discogs &lt;id&gt; tag.
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * The masters title obtained from discogs &lt;title&gt; tag.
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * The artists involved.
	 * @return the artists
	 */
	public Set<Artist> getArtists() {
		return artists;
	}

	/**
	 * The artist(s) as displayed on the cover. Important for collaborations e.g. &apos;Prince And The New Power Generation&apos;
	 * @return
	 */
	public String getDisplayArtist() {
		return displayArtist;
	}

	/**
	 * The tracks.
	 * @return the artists
	 */
	public List<Track> getTracklist() {
		return tracklist;
	}

	/**
	 * The ids of the artists involved. Useful in ReleaseContentHandler if we decide not to persist a release.
	 * @return the artists
	 */
	public List<Long> getArtistIds() {
		return artistIds;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setArtists(Set<Artist> artists) {
		this.artists = artists;
	}

	public void setDisplayArtist(String displayArtist) {
		this.displayArtist = displayArtist;
	}

	public void setTracklist(List<Track> tracklist) {
		this.tracklist = tracklist;
	}

	public void setArtistIds(List<Long> artistIds) {
		this.artistIds = artistIds;
	}

	@Override
	public String toString() {
		return "Release [id=" + String.format("%,d", id) + ", title=" + title +  ", displayArtist=" + displayArtist + ", artists=" + artists + ", tracklist=" + tracklist + "]";
	}
}