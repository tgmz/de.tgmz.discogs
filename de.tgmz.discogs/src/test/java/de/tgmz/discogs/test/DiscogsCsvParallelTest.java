/*********************************************************************
* Copyright (c) 24.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.test;

import java.io.IOException;
import java.util.concurrent.ForkJoinTask;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.tgmz.discogs.load.Action;
import de.tgmz.discogs.load.ActionFactory;
import de.tgmz.discogs.load.Mode;
import de.tgmz.discogs.load.persist.csv.Table;

public class DiscogsCsvParallelTest extends DiscogsCsvTest {
	@BeforeClass
	public static void setupOnce() throws IOException {
		DiscogsTest.setupOnce();
		
		init();
		
		ActionFactory af = ActionFactory.getInstance().forTables(Table.values());

		ForkJoinTask.invokeAll(af.create(Action.LOAD_NO_PK, Mode.PARALLEL, dataDir.toString()));
		ForkJoinTask.invokeAll(af.create(Action.PRIMARY_KEY, Mode.SEQUENTIAL));
		ForkJoinTask.invokeAll(af.create(Action.RECONCILE, Mode.DEPENDING));
		ForkJoinTask.invokeAll(af.create(Action.INDEX, Mode.SUMMUP));
		ForkJoinTask.invokeAll(af.create(Action.CONSTRAINT, Mode.SEQUENTIAL));
	}
	
	@AfterClass
	public static void teardownOnce() throws IOException {
		DiscogsTest.teardownOnce();
	}
}
