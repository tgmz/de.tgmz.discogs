/*********************************************************************
* Copyright (c) 08.12.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.relevance;

import java.util.Arrays;

import org.apache.commons.lang3.Strings;

public class RelevanceService {
	private static final RelevanceService INSTANCE = new RelevanceService();
	
	private String[] relevantRoles;
	
	private RelevanceService() {
	}

	public static RelevanceService getInstance() {
		return INSTANCE;
	}

	public void setRelevantRoles(String... relevantRoles) {
		this.relevantRoles = relevantRoles != null ? Arrays.copyOf(relevantRoles, relevantRoles.length) : null;
	}
	
	public boolean isRelevant(String role) {
		return relevantRoles == null || Strings.CS.containsAny(role, relevantRoles);
	}

}
