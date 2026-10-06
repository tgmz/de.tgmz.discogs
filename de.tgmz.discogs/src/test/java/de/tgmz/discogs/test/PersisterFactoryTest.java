/*********************************************************************
* Copyright (c) 05.10.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.test;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.PrimaryEntity;
import de.tgmz.discogs.load.persist.IPersister;
import de.tgmz.discogs.load.persist.PersisterException;
import de.tgmz.discogs.load.persist.PersisterFactoryImpl;

public class PersisterFactoryTest {
	private static final String CSV_PROP = "DISCOGS_CSV_TARGET";

	@SuppressWarnings("serial")
	private static class InvalidEntity extends PrimaryEntity {
	}
	
	@After
	public void teardown() {
		System.clearProperty(CSV_PROP);
	}
	
	@Test
	public void testJakarta() throws PersisterException {
		PersisterFactoryImpl.reload();
		
		IPersister<Artist> ip = PersisterFactoryImpl.getInstance().create(Artist.class, x -> true);
		
		assertTrue(ip instanceof de.tgmz.discogs.load.persist.jakarta.ArtistPersister);
	}
	
	@Test
	public void testCsv() throws PersisterException {
		System.setProperty(CSV_PROP, System.getProperty("java.io.tmpdir"));
		
		PersisterFactoryImpl.reload();
		
		IPersister<Artist> ip = PersisterFactoryImpl.getInstance().create(Artist.class, x -> true);
		
		assertTrue(ip instanceof de.tgmz.discogs.load.persist.csv.ArtistPersister);
	}
	
	@Test(expected = PersisterException.class)
	public void testInvalidPersistableClass() throws PersisterException {
		PersisterFactoryImpl.getInstance().create(InvalidEntity.class, x -> true);
	}
}
