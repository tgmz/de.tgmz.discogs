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

import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public enum DiscogsFile {
	ARTISTS("artists.xml")
	, LABELS("labels.xml")
	, MASTERS("masters.xml")
	, RELEASES("releases.xml"),
	;
	
	private static final String DISCOGS_ID = "DISCOGS_ID";
	private static final String DISCOGS_DIR = "DISCOGS_DIR";
	private static final String DISCOGS_URL = "DISCOGS_URL";
	
	private static Map<String, String> env;
	
	private String file;

	private DiscogsFile(String file) {
		this.file = file;
	}

	public String getFileName() {
		return getEnv(DISCOGS_ID) + file;
	}

	public File getFile() {
		return new File(getEnv(DISCOGS_DIR), getFileName());
	}

	public String getZipFileName() {
		return getFileName() + ".gz";
	}

	public File getZipFile() {
		return new File(getEnv(DISCOGS_DIR), getZipFileName());
	}

	public static String getKey() {
		return getEnv(DISCOGS_ID);
	}

	public static String getBaseUrl() {
		return getEnv(DISCOGS_URL);
	}
	
	private static String getEnv(String key) {
		if (env == null) {
			env = new TreeMap<>();

			Calendar cal = Calendar.getInstance();
			
			String year = String.format("%4d", cal.get(Calendar.YEAR));
			
			String id = "discogs_" 
					+ year
					+ String.format("%02d", cal.get(Calendar.MONTH) + 1) 
					+ "01_";

			env.put(DISCOGS_ID, id);
			
			env.put(DISCOGS_DIR, System.getProperty(DISCOGS_DIR, System.getProperty("user.home")));

			env.put(DISCOGS_URL, System.getProperty(DISCOGS_URL, "https://discogs-data-dumps.s3-us-west-2.amazonaws.com/data/" + year + "/"));
		}
		
		return env.get(key);
	}
}
