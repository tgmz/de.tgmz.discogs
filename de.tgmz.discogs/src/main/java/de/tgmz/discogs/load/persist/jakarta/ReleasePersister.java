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

import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.load.factory.ReleaseFactory;

public class ReleasePersister extends AbstractJakartaPersistable<Release> {
	public ReleasePersister(Predicate<Release> filter, int threshold) {
		super(filter, new ReleaseFactory(), threshold);
	}
}
