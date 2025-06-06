/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.domain.Discogs;

public class MasterLoader {
	private static final Logger LOG = LoggerFactory.getLogger(MasterLoader.class);
	
	private Predicate<Discogs> p;
	
	public MasterLoader() {
		this (d -> true);
	}
	
	public MasterLoader(Predicate<Discogs> p) {
		this.p = p;
	}
	
	public void run() {
		
	}
}
