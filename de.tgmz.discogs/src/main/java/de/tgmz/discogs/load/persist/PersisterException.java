/*********************************************************************
* Copyright (c) 05.10.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist;

public class PersisterException extends Exception {
	private static final long serialVersionUID = 878559256202073921L;

	public PersisterException(String message) {
		super(message);
	}

	public PersisterException(String message, Throwable cause) {
		super(message, cause);
	}
}
