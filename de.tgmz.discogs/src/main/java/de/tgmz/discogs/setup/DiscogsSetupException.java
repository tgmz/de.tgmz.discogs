/*********************************************************************
* Copyright (c) 17.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.setup;

public class DiscogsSetupException extends RuntimeException {
	private static final long serialVersionUID = 9198346977338818150L;

	public DiscogsSetupException() {
		super();
	}

	public DiscogsSetupException(Exception e) {
		super(e);
	}
}
