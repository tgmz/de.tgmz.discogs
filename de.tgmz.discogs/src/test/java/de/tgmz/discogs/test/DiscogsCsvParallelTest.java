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

public class DiscogsCsvParallelTest extends DiscogsCsvTest {
	@BeforeClass
	public static void setupOnce() throws IOException {
		DiscogsTest.setupOnce();
		
		init();

		ForkJoinTask.invokeAll(ActionFactory.getInstance().create(Action.LOAD, Mode.PARALLEL, dataDir.toString()));
		ForkJoinTask.invokeAll(ActionFactory.getInstance().create(Action.RECONCILE, Mode.DEPENDING));
		ForkJoinTask.invokeAll(ActionFactory.getInstance().create(Action.OPTIMIZE, Mode.SUMMUP));
		ForkJoinTask.invokeAll(ActionFactory.getInstance().create(Action.VALIDATE, Mode.SEQUENTIAL));
	}
	
	@AfterClass
	public static void teardownOnce() throws IOException {
		DiscogsTest.teardownOnce();
	}
}
