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

public abstract class PredecessorAwareAction extends RecursiveAction {
	private static final long serialVersionUID = -3804048897401974703L;
	private static final Logger LOG = LoggerFactory.getLogger(PredecessorAwareAction.class);
	private List<RecursiveAction> predecessors;
	private Table table;

	protected PredecessorAwareAction(Table table) {
		super();
		this.table = table;
		
		predecessors = new LinkedList<>();
	}

	@Override
	public void compute() {
		// Wait for predecessors to finish
		try {
			for (RecursiveAction ra : predecessors) {
				LOG.debug("Await {} ({})", ra, ra.state());
			
				ra.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Predecessor failed", e);
			
			Thread.currentThread().interrupt();
		}
		
		execute();
	}
	
	protected abstract void execute();

	public Table getTable() {
		return table;
	}

	public List<RecursiveAction> getPredecessors() {
		return predecessors;
	}

	@Override
	public String toString() {
		return "PredecessorAwareAction [table=" + table + "]";
	}
}
