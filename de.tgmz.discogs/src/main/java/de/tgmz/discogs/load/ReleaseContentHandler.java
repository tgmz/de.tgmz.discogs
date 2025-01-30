package de.tgmz.discogs.load;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.domain.Track;

public class ReleaseContentHandler extends DiscogsContentHandler {
	private static final String TAG_RELEASE = "release";
	private static final String TAG_ARTISTS = "artists";
	private static final String TAG_ARTIST = "artist";
	private Release release;
	private Artist artist;
	private Track track;
	private List<String> displayArtists;
	private List<String> displayJoins;
	private boolean inTrack;
	private boolean complete;

	public void run(InputStream is) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(is));
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		
		inTrack = false;
		complete = false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		super.startElement(uri, localName, qName, attributes);
		
		if (complete) {
			return;
		}
		
		switch (qName) {
		case TAG_RELEASE:
			release = new Release();
			
			release.setId(Long.parseLong(attributes.getValue("id")));
			
			break;
		case TAG_ARTISTS:
			if (!inTrack) {
				displayArtists = new ArrayList<>();
				displayJoins = new ArrayList<>();
			}
			
			break;
		case TAG_ARTIST:
			artist = new Artist();
			
			break;
		case "tracklist":
			inTrack = true;
			
			release.setTracklist(new LinkedList<>());
			
			break;
		case "track":
			track = new Track();
			
			break;
		default:
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		
		if (complete && !TAG_RELEASE.equals(qName)) {
			return;
		}
		
		switch (qName) {
		case "id":
			if (TAG_ARTIST.equals(stack.peek())) {
				artist.setId(Long.valueOf(getChars()));
			}
			
			break;
		case "name":
			if (!inTrack) {
				displayArtists.add(getChars());
			}
			
			break;
		case "anv":
			if (!inTrack) {
				displayArtists.set(displayArtists.size() - 1, getChars());
			}
			
			break;
		case TAG_ARTIST:
			if (!stack.contains("extraartists")) {
				if (inTrack) {
					track.getArtistIds().add(artist.getId());
				} else {
					release.getArtistIds().add(artist.getId());
				}
			}
			
			break;
		case "join":
			if (!inTrack) {
				displayJoins.add(getChars());
			}
			
			break;
		case "position":
			track.setPosition(getChars());
			
			break;
		case TAG_ARTISTS:
			if (!inTrack) {
				release.setDisplayArtist(getDisplayArtist(displayArtists, displayJoins));
			}
			
			break;
		case "duration":
			track.setDuration(getChars());
			
			break;
		case "title":
			switch (stack.peek()) {
			case TAG_RELEASE:
				if (release.getTitle() == null) {
					release.setTitle(getChars());
				}
				
				break;
			case "track":
				track.setTitle(StringUtils.left(getChars(), MAX_LENGTH_TITLE));
				
				release.getTracklist().add(track);
				
				break;
			default:
				break;
			}
				
			break;
		case "tracklist":
			complete = true;
			inTrack = false;
			
			break;
		
		case TAG_RELEASE:
			if (release.getId() % 10_000 == 0 && LOG.isInfoEnabled()) {
				LOG.info("Save {}", release);
			}
			
			release.setTitle(StringUtils.left(release.getTitle(),  MAX_LENGTH_TITLE));
			
			setArtists();
				
			LOG.debug("Save {}", release);
				
			save(release);
			
			complete = false;
			
			break;
		default:
		}
	}
	@Override
	public void endDocument() throws SAXException {
		Object o = em.createQuery("SELECT COUNT(*) FROM Release").getSingleResult();
		
		long count = (o == null) ? 0L : (long) o;
		
		LOG.info("{} releases inserted/updated", String.format("%,d", count));
		
		super.endDocument();
	}
	private void setArtists() {
		release.setArtists(getArtists(release.getArtistIds()));
		
		for (Track t : release.getTracklist()) {
			t.setArtists(getArtists(t.getArtistIds()));
		}
	}
	private Set<Artist> getArtists(List<Long> artistIds) {
		Set<Artist> result = new HashSet<>();
		
		for (long l : artistIds) {
			Artist a0 = em.find(Artist.class, l);
		
			if (a0 == null) {
				LOG.debug("Artist {} not found", l);
			} else {
				result.add(a0);
			}
		}
		
		return result;
	}
}
