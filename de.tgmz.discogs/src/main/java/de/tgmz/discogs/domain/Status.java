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

public enum Status {
	ACCEPTED("Accepted");
	
	private String name;

	private Status(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static Status byName(String name) {
		for (Status s : values()) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		
		throw new NoSuchElementException(name);
	}
}
