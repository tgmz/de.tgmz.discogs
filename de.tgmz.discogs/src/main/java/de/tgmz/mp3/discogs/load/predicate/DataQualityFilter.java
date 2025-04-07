/*********************************************************************
* Copyright (c) 03.04.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.mp3.discogs.load.predicate;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.DataQuality;
import de.tgmz.discogs.domain.Discogs;

/**
 * Save only masters/releases with given dataQualities.
 */
public class DataQualityFilter implements Predicate<Discogs> {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(DataQualityFilter.class);
	private List<DataQuality> dq;
	
	public DataQualityFilter(DataQuality... dq) {
		this.dq = List.of(dq);
	}
	
	@Override
	public boolean test(Discogs d) {
		return dq.contains(d.getDataQuality());  
	}
}
