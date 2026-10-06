/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.jakarta;

import java.util.function.Predicate;

import de.tgmz.discogs.domain.Master;
import de.tgmz.discogs.load.factory.IFactory;
import de.tgmz.discogs.load.factory.MasterFactory;

public class MasterPersister extends AbstractJakartaPersistable<Master> {
	private Predicate<Master> filter;
	private IFactory<Master> mf;
	
	public MasterPersister(Predicate<Master> filter) {
		this.filter = filter;
		
		mf = new MasterFactory();
	}
	
	@Override
	public IFactory<Master> getFactory() {
		return mf;
	}

	@Override
	public Predicate<Master> getFilter() {
		return filter;
	}

}
