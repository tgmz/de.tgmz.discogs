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

import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.load.factory.IFactory;
import de.tgmz.discogs.load.factory.ReleaseFactory;

public class ReleasePersistable implements IPersistable<Release> {
	private Predicate<Release> filter;
	
	public ReleasePersistable() {
		this(x -> true);
	}
	
	public ReleasePersistable(Predicate<Release> filter) {
		this.filter = filter;
	}

	@Override
	public IFactory<Release> getFactory() {
		return new ReleaseFactory();
	}

	@Override
	public Predicate<Release> getFilter() {
		return filter;
	}
}
