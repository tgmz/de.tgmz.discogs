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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public final class PersisterFactoryImpl implements IPersisterFactory {
	private static final Logger LOG = LoggerFactory.getLogger(PersisterFactoryImpl.class);

	private static PersisterFactoryImpl instance;

	private Map<String, Class<IPersistable<?>>> persisters;

	/**
	 * Private constructor for security reasons
	 */
	private PersisterFactoryImpl() {
		persisters = new HashMap<>();
		
		String persisterPackage = this.getClass().getPackageName() +  
				(System.getProperty("DISCOGS_CSV_TARGET") != null ? ".csv" : ".jakarta");
		
		try (ScanResult scanResult = new ClassGraph().acceptPackagesNonRecursive(persisterPackage).scan()) {
			for (ClassInfo ci : scanResult.getClassesImplementing(IPersistable.class).filter(x -> !x.isAbstract())) {
				Entry<String, Class<IPersistable<?>>> cp = computePersistable(ci);
								
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
	public <T> IPersistable<T> create(Class<T> persistableClass, Predicate<T> filter) throws PersisterException {
		Class<?> clz = persisters.get(persistableClass.getCanonicalName());

		if (clz == null) {
			throw new PersisterException(String.format("No persistable defined for %s", persistableClass));
		}
		
		try {
			return (IPersistable<T>) clz.getDeclaredConstructor(Predicate.class).newInstance(filter);
		} catch (ReflectiveOperationException e) {
			throw new PersisterException(String.format("Cannot create persistable for %s", persistableClass), e);
		}
	}
	
	public static void reset() {
		instance = null;
	}
	
	private Map.Entry<String, Class<IPersistable<?>>> computePersistable(ClassInfo ci) {
		@SuppressWarnings("unchecked") //Safe: We know from the filtering in CTR that ci implements IPersistable
		Class<IPersistable<?>> clz = (Class<IPersistable<?>>) ci.loadClass();

		if (clz.getGenericSuperclass() instanceof ParameterizedType pt) {
			for (Type t : pt.getActualTypeArguments()) {
				if (t instanceof Class<?> pe && PrimaryEntity.class.isAssignableFrom(pe)) {
					LOG.info("Using {} for persisting {}", clz, pe);
						
					return new AbstractMap.SimpleEntry<>(pe.getName(), clz);
				}
			}
		}
		
		return null;
	}
}
