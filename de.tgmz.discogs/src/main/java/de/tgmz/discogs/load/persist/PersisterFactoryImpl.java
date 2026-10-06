/*********************************************************************
* Copyright (c) 04.10.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.domain.PrimaryEntity;
import de.tgmz.discogs.domain.Release;

public final class PersisterFactoryImpl implements IPersisterFactory {
	private static final Logger LOG = LoggerFactory.getLogger(PersisterFactoryImpl.class);

	private static final Map<Class<? extends PrimaryEntity>, Class<? extends IPersister<?>>> PERSISTERS = new HashMap<>();

	private static final PersisterFactoryImpl INSTANCE = new PersisterFactoryImpl();

	/**
	 * Private constructor for security reasons
	 */
	private PersisterFactoryImpl() {
		reload();
	}
	
	public static IPersisterFactory getInstance() {
		return INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PrimaryEntity> IPersister<T> create(Class<T> entityClass, Predicate<T> filter) throws PersisterException {
		Class<?> persitableClass = PERSISTERS.get(entityClass);

		if (persitableClass == null) {
			throw new PersisterException(String.format("No persistable defined for %s", entityClass));
		}
		
		try {
			return (IPersister<T>) persitableClass.getDeclaredConstructor(Predicate.class).newInstance(filter);
		} catch (ReflectiveOperationException e) {
			throw new PersisterException(String.format("Cannot create persistable for %s", entityClass), e);
		}
	}
	
	public static void reload() {
		PERSISTERS.clear();
		
		if (System.getProperty("DISCOGS_CSV_TARGET") != null) {
			PERSISTERS.put(Artist.class,  de.tgmz.discogs.load.persist.csv.ArtistPersister.class);
			PERSISTERS.put(Label.class,   de.tgmz.discogs.load.persist.csv.LabelCsvPersister.class);
			PERSISTERS.put(Master.class,  de.tgmz.discogs.load.persist.csv.MasterCsvPersister.class);
			PERSISTERS.put(Release.class, de.tgmz.discogs.load.persist.csv.ReleaseCsvPersister.class);
		} else {
			PERSISTERS.put(Artist.class,  de.tgmz.discogs.load.persist.jakarta.ArtistPersister.class);
			PERSISTERS.put(Label.class,   de.tgmz.discogs.load.persist.jakarta.LabelPersister.class);
			PERSISTERS.put(Master.class,  de.tgmz.discogs.load.persist.jakarta.MasterPersister.class);
			PERSISTERS.put(Release.class, de.tgmz.discogs.load.persist.jakarta.ReleasePersister.class);
		}
		
		PERSISTERS.forEach((x,y) -> LOG.info("Using {} for persisting {}", y, x));
	}
}
