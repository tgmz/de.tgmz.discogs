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

public enum Action {
	LOAD			// Create table with primary key and load it
	, LOAD_NO_PK	// Create table with _no_ primary key and load it
	, RECONCILE		// Heal inconsistencies
	, PRIMARY_KEY	// Create primary keys
	, INDEX			// Create indexes
	, CONSTRAINT	// Add constraints
}
