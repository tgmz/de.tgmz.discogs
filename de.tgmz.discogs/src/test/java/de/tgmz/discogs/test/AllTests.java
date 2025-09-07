/*********************************************************************
* Copyright (c) 17.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.test;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({SetupTest.class, DiscogsTest.class })
public class AllTests {
	private static final String JDBC_PROTOCOL = "jdbc:h2:file:";
	private static final String JDBC_DATA_DIR = System.getProperty("java.io.tmpdir") + File.separatorChar + "discogs_test";
	private static final String JDBC_PROPERTIES = ";MODE=DB2;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE";
	
	public static final String JDBC_URL = JDBC_PROTOCOL + JDBC_DATA_DIR + JDBC_PROPERTIES;
}
