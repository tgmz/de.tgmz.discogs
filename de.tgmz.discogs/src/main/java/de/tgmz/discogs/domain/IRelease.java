package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.Set;

/**
 * 
 */
public interface IRelease extends Serializable {
	long getId();
	String getTitle();
	String getDisplayArtist();
	Set<Artist> getArtists();
}
