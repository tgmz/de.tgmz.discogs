/*********************************************************************
* Copyright (c) 11.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.ExtraArtist;
import jakarta.persistence.EntityManager;

public class ExtraArtistFactory implements IFactory<ExtraArtist> {
	private static final Logger LOG = LoggerFactory.getLogger(ExtraArtistFactory.class);
	
	@Override
	public ExtraArtist get(EntityManager em, ExtraArtist draft) {
		return findOrCreate(em, draft);
	}
	
	private ExtraArtist findOrCreate(EntityManager em, ExtraArtist draft) {
		Artist a = em.find(Artist.class, draft.getArtist().getId());
		
		if (a == null) {
			a = draft.getArtist();

			LOG.trace("Artist {} not present, creating...", a);
			
			em.persist(a);
		}
		
		String role = draft.getRole();
		
		// This trick let us use a variable in a lambda: The array eaid is final, eaid[0] is not.
		final Long[] eaid = new Long[1];
		
		ExtraArtist ea;
		
		// Bypass Hibernate: It yields super-strange primary key violation when it tries to INSERT an artist_member 
		// when we only want to select an ExtraArtist by artist.id and role (???)
    	em.runWithConnection((Connection conn) -> {
    		try (PreparedStatement pstmt = conn.prepareStatement("SELECT ea.ID FROM EXTRAARTIST ea WHERE ea.ARTIST_ID = ? AND ea.ROLE = ?")) {
    			pstmt.setLong(1, draft.getArtist().getId());
    			pstmt.setString(2, role);
    		
    			ResultSet rs = pstmt.executeQuery();
    		
    			if (rs.next()) {
    				eaid[0] = rs.getLong(1);
    			}
    		
    			rs.close();
    		}
    	});

		if (eaid[0] == null) {
			LOG.trace("ExtraArtist {} not present, creating...", draft);
			
			ea = draft;
			
			ea.setArtist(a);
			
			em.persist(ea);
		} else {
			ea = em.find(ExtraArtist.class, eaid[0]);
		}
		
		return ea;
	}
}
