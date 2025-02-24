/*********************************************************************
* Copyright (c) 24.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.NoSuchElementException;

public enum DataQuality {
	COMPLETE_AND_CORRECT("Complete and Correct")
	, CORRECT("Correct")
	, NEEDS_MINOR_CHANGES("Needs Minor Changes")
	, NEEDS_MAJOR_CHANGES("Needs Major Changes")
	, ENTIRELY_INCORRECT("Entirely Incorrect")
	, NEEDS_VOTE("Needs Vote")
	;
	
	private String name;

	private DataQuality(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static DataQuality byName(String name) {
		for (DataQuality dq : values()) {
			if (dq.name.equals(name)) {
				return dq;
			}
		}
		
		throw new NoSuchElementException(name);
	}
}
