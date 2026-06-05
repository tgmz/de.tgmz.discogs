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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.persist.csv.Table;
import de.tgmz.discogs.logging.LogUtil;

public class DatabaseAction extends RecursiveAction {
	private static final long serialVersionUID = -3804048897401974703L;
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseAction.class);
	private Table table;
	private List<String> sqls;
	private List<RecursiveAction> predecessors;

	public DatabaseAction(Table table, List<String> sqls) {
		super();
		
		this.table = table;
		this.sqls = sqls;
		
		predecessors = new LinkedList<>();
	}
	public List<String> getSqls() {
		return sqls;
	}
	
	@Override
	public void compute() {
		await();
		
		try (Connection conn = DriverManager
				.getConnection(System.getProperty("jakarta.persistence.jdbc.url")
						, System.getProperty("jakarta.persistence.jdbc.user")
						, System.getProperty("jakarta.persistence.jdbc.password"));
				Statement stmt = conn.createStatement()) {
			long start = System.currentTimeMillis();
			
			for (String sql : sqls) {
				LOG.info("{}: Execute {}", table, sql);

				stmt.addBatch(sql);
			}

			int affected = IntStream.of(stmt.executeBatch()).sum();
			
			if (affected > 0 && LOG.isInfoEnabled()) {
				LOG.info("{}: {} rows were affected in {}", table, String.format("%,d", affected), LogUtil.formatDuration(start, System.currentTimeMillis()));
			}
		} catch (SQLException e) {
			LOG.error("Execution failed: {}", e.getMessage());
		}
	}
	
	protected void await() {
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
	}

	public List<RecursiveAction> getPredecessors() {
		return predecessors;
	}

	public Table getTable() {
		return table;
	}
	
	@Override
	public String toString() {
		return "DatabaseAction [table=" + table + "]";
	}
}
