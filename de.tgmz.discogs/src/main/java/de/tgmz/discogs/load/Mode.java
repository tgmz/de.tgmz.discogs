/*********************************************************************
* Copyright (c) 26.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

public enum Mode {
	PARALLEL		// Run all actions parallel in arbitrary order
	, SEQUENTIAL	// Run one action after another in sequence defined by Table enum
	, DEPENDING		// Like parallel but honor dependencies in Table enum
}
