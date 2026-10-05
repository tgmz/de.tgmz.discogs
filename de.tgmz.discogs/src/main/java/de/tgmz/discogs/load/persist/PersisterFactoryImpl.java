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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.PrimaryEntity;
import de.tgmz.discogs.load.persist.jakarta.IPersistable;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public final class PersisterFactoryImpl implements IPersisterFactory {
	private static final Logger LOG = LoggerFactory.getLogger(PersisterFactoryImpl.class);

	private static PersisterFactoryImpl instance = new PersisterFactoryImpl();

	private Map<String, Class<?>> persisters;

	/**
	 * Private constructor for security reasons
	 */
	private PersisterFactoryImpl() {
		persisters = new HashMap<>();
		
		String persisterPackage = this.getClass().getPackageName() +  
				(System.getProperty("DISCOGS_CSV_TARGET") != null ? ".csv" : ".jakarta");
		
		try (ScanResult scanResult = new ClassGraph().acceptPackagesNonRecursive(persisterPackage).scan()) {
			for (ClassInfo ci : scanResult.getClassesImplementing(IPersistable.class).filter(x -> !x.isAbstract())) {
				Entry<String, Class<?>> cp = computePersistable(ci);
								
				if (cp != null) {
					persisters.put(cp.getKey(), cp.getValue());
				}
			}
		}
	}
	
	public static IPersisterFactory getInstance() {
		if (instance == null) {
			instance = new PersisterFactoryImpl();
		}
		
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T> IPersistable<T> create(Class<T> persistableClass, Predicate<T> filter) {
		Class<?> clz = persisters.get(persistableClass.getCanonicalName());
		
		try {
			return (IPersistable<T>) clz.getDeclaredConstructor(Predicate.class).newInstance(filter);
		} catch (ReflectiveOperationException e) {
			LOG.error("Cannot create peristable for {}", persistableClass, e);
		}
		
		return null;
	}
	
	public static void reset() {
		instance = null;
	}
	
	private Map.Entry<String, Class<?>> computePersistable(ClassInfo ci) {
		Class<?> clz = ci.loadClass();
		
		do {
			if (clz.getGenericSuperclass() instanceof ParameterizedType pt) {
				for (Type t : pt.getActualTypeArguments()) {
					String s = t.getTypeName();

					try {
						if (PrimaryEntity.class.isAssignableFrom(Class.forName(s))) {
							LOG.info("Using {} for persisting {}", clz, s);
						
							return new AbstractMap.SimpleEntry<>(s, clz);
						}
					} catch (ClassNotFoundException e) {
						LOG.error("Persistable class not found", e);
					}
				}
			}
			
			clz = clz.getSuperclass();
		} while (clz != Object.class);
		
		return null;
	}
}
