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

import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.load.factory.IFactory;
import de.tgmz.discogs.load.factory.MasterFactory;

public class MasterPersistable implements IPersistable<Master> {
	private Predicate<Master> filter;
	
	public MasterPersistable() {
		this(x -> true);
	}

	public MasterPersistable(Predicate<Master> filter) {
		this.filter = filter;
	}
	
	@Override
	public IFactory<Master> getFactory() {
		return new MasterFactory();
	}

	@Override
	public Predicate<Master> getFilter() {
		return filter;
	}

}
