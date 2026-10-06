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

import java.util.function.Predicate;

import de.tgmz.discogs.domain.PrimaryEntity;

public interface IPersisterFactory {
	<T extends PrimaryEntity> IPersister<T> create(Class<T> entityClass, Predicate<T> filter) throws PersisterException;
}
