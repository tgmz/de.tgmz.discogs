/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;

public interface IPersistable<T> {
	static final Logger LOG = LoggerFactory.getLogger(IPersistable.class);
	
	IFactory<T> getFactory();
	Predicate<T> getFilter();
	
	default	int save(EntityManager em, T o) {
		if (getFilter().test(o)) {
			LOG.debug("Save {}", o);
			
			em.merge(getFactory().get(em, o));
			
			return 1;
		} else {
			LOG.debug("Ignore {}", o);
			
			return 0;
		}
	}
}
