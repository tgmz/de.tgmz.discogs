/*********************************************************************
* Copyright (c) 24.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.persist.csv.Table;

public class LoadAction extends RecursiveAction {
	private static final long serialVersionUID = -3804048897401974703L;
	private static final Logger LOG = LoggerFactory.getLogger(LoadAction.class);
	private CsvLoader csvl;
	private Table table;
	private List<RecursiveAction> predecessors;

	public LoadAction(CsvLoader csvl, Table t) {
		super();
		this.csvl = csvl;
		this.table = t;
		
		predecessors = new LinkedList<>();
	}

	@Override
	protected void compute() {
		try {
			// Wait for predecessors to finish
			for (RecursiveAction ra : predecessors) {
				ra.get();
			}
		
			csvl.loadTable(table, false);
		} catch (ExecutionException | InterruptedException e) {
			LOG.error("Predecessor failed, aborting", e);
			
			Thread.currentThread().interrupt();
		}
	}

	public Table getTable() {
		return table;
	}

	public List<RecursiveAction> getPredecessors() {
		return predecessors;
	}
}
