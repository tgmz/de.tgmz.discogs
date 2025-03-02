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
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public enum DiscogsFile {
	ARTISTS(true, "artists.xml")
	, LABELS(true, "labels.xml")
	, MASTERS(true, "masters.xml")
	, RELEASES(true, "releases.xml")
	, CHECKSUM(false, "CHECKSUM.txt")
	,
	;
	
	public static final String DISCOGS_DIR = "DISCOGS_DIR";
	public static final String DISCOGS_URL = "DISCOGS_URL";
	
	private static Map<String, String> env;
	
	private boolean zipped;
	private String file;

	private DiscogsFile(boolean zipped, String file) {
		this.zipped = zipped;
		this.file = file;
	}

	public boolean isZipped() {
		return zipped;
	}

	public String getFileName() {
		String s = LbrFactory.getInstance().getContents(file).getKey();
		
		return zipped ? StringUtils.removeEnd(s, ".gz") : s;
	}

	public File getFile() {
		return new File(getEnv(DISCOGS_DIR), getFileName());
	}

	public String getRemote() {
		return getEnv(DISCOGS_URL) + LbrFactory.getInstance().getContents(file).getKey();
	}

	public String getZipFileName() {
		return getFileName() + ".gz";
	}

	public File getZipFile() {
		return new File(getEnv(DISCOGS_DIR), getZipFileName());
	}

	private static String getEnv(String key) {
		if (env == null) {
			env = new TreeMap<>();

			env.put(DISCOGS_DIR, System.getProperty(DISCOGS_DIR, System.getProperty("user.home")));

			env.put(DISCOGS_URL, System.getProperty(DISCOGS_URL, "https://discogs-data-dumps.s3-us-west-2.amazonaws.com/"));
		}
		
		return env.get(key);
	}
}
