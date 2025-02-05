package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
	@ElementCollection
	@CollectionTable(name = "Variations" , joinColumns = @JoinColumn(name = "artist_id"))
	private Set<String> variations;

	public Artist() {
		super();
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

	/**
	 * Variation of the artists name. Useful for finding typos etc.
	 * @return the id
	 */
	public Set<String> getVariations() {
		return variations;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVariations(Set<String> variations) {
		this.variations = variations;
	}

	@Override
	public String toString() {
		return "Artist [id=" + String.format("%,d", id) + ", name=" + name + ", variations=" + variations + "]";
	}
}