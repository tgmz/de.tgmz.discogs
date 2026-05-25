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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.tgmz.discogs.load.CsvLoader;
import de.tgmz.discogs.load.LoadAction;
import de.tgmz.discogs.load.persist.csv.Table;

public class DiscogsCsvParallelTest extends DiscogsCsvTest {
	@BeforeClass
	public static void setupOnce() throws IOException {
		DiscogsTest.setupOnce();
		
		init();

		CsvLoader csvl = new CsvLoader(dataDir.toString());
		
		List<LoadAction> loadActions = new LinkedList<>();
		
		Stream.of(Table.values()).forEach(t -> loadActions.add(new LoadAction(csvl, t)));
		
		for (LoadAction la : loadActions) {
			List<Table> dependsOn = la.getTable().getDependsOn();
			
			la.getPredecessors().addAll(loadActions.stream().filter(la0 -> dependsOn.contains(la0.getTable())).toList());
		}
		
		ForkJoinTask.invokeAll(loadActions);

		csvl.verify();
	}
	
	@AfterClass
	public static void teardownOnce() throws IOException {
		DiscogsTest.teardownOnce();
	}
}
