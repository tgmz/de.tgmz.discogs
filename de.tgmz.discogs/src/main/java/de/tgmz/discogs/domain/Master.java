package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.processing.Generated;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Master entity.
 */
@Entity
@Table(indexes = {
	@Index(columnList = "title", name = "title_idx"),
	@Index(columnList = "displayArtist", name = "displayArtist_idx"),
})
public class Master implements IRelease {
	@Transient
	private static final long serialVersionUID = -5230886354906404806L;
	@Id
	private long id;
	private String title;
	private Integer published;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<Artist> artists;
	@Column(length = 512)
	private String displayArtist;

	public Master() {
		super();
	}

	/**
	 * The masters id obtained from discogs &lt;id&gt; tag.
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
	 * The year the master was published. Obtained from discogs &lt;year&gt; tag. Renamed from year because
	 * year is a reseved word in most databases.
	 * @return the year
	 */
	public Integer getPublished() {
		return published;
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

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPublished(Integer year) {
		this.published = year;
	}

	public void setArtists(Set<Artist> artists) {
		this.artists = artists;
	}

	public void setDisplayArtist(String displayArtist) {
		this.displayArtist = displayArtist;
	}

	@Override
	public String toString() {
		return "Master [id=" + String.format("%,d", id) + ", title=" + title + ", year=" + published + ", displayArtist=" + displayArtist + ", artists=" + artists + "]";
	}
}