/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.io.Serializable;
import java.util.Set;

/**
 * Common attribute for Master and Release.
 */
public interface IRelease extends Serializable {
	long getId();
	String getTitle();
	String getDisplayArtist();
	Set<Artist> getArtists();
}
