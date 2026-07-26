/*********************************************************************
* Copyright (c) 07.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import java.util.NoSuchElementException;

public enum IdentType {
	BARCODE("Barcode")
	, LABEL_CODE("Label Code")
	, MATRIX_RUNOUT("Matrix / Runout")
	, MASTERING_SID_CODE("Mastering SID Code")
	, MOULD_SID_CODE("Mould SID Code")
	, PRESSING_PLANT_ID("Pressing Plant ID")
	, DISTRIBUTION_CODE("Distribution Code")
	, PRICE_CODEX("Price Code")
	, SPARS_CODEX("SPARS Code")
	, DESPOSITO_LEGALX("Depósito Legal")
	, ASIN("ASIN")
	, ISRC("ISRC")
	, RIGHTS_SOCIETY("Rights Society")
	, OTHER("Other")
	;
	
	private String name;

	private IdentType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static IdentType byName(String name) {
		for (IdentType dq : values()) {
			if (dq.name.equals(name)) {
				return dq;
			}
		}
		
		throw new NoSuchElementException(name);
	}
}
