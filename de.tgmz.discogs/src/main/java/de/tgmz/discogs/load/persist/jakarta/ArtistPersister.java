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

import de.tgmz.discogs.domain.Artist;
import de.tgmz.discogs.load.factory.ArtistFactory;

public class ArtistPersister extends AbstractJakartaPersistable<Artist> {
	public ArtistPersister(Predicate<Artist> filter, int threshold) {
		super(filter, new ArtistFactory(), threshold);
	}
}
