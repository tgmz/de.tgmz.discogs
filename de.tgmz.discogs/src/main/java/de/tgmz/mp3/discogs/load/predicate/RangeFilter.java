/*********************************************************************
* Copyright (c) 04.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.mp3.discogs.load.predicate;

import java.util.function.Predicate;

import de.tgmz.discogs.domain.Discogs;

public class RangeFilter implements Predicate<Discogs> {
	private long min;
	private long max;
	
	public RangeFilter(long min, long max) {
		super();
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean test(Discogs d) {
		return d.getId() > min && d.getId() < max;
	}
}
