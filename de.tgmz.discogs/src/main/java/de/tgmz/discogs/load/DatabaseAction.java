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
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.persist.csv.Table;
import de.tgmz.discogs.logging.LogUtil;

public class DatabaseAction extends PredecessorAwareAction {
	private static final long serialVersionUID = -3804048897401974703L;
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseAction.class);
	private List<String> sqls;

	public DatabaseAction(Table table, List<String> sqls) {
		super(table);
		this.sqls = sqls;
	}
	public List<String> getSqls() {
		return sqls;
	}
	@Override
	protected void execute() {
		try (Connection conn = DriverManager
				.getConnection(System.getProperty("jakarta.persistence.jdbc.url")
						, System.getProperty("jakarta.persistence.jdbc.user")
						, System.getProperty("jakarta.persistence.jdbc.password"));
				Statement stmt = conn.createStatement()) {
			long start = System.currentTimeMillis();
			
			for (String sql : sqls) {
				LOG.info("Execute {}", sql);

				stmt.addBatch(sql);
			}

			int[] affected = stmt.executeBatch();
			
			StringJoiner sj = new StringJoiner(" / ");
			
			for (int i : affected) {
				if (i > 0) {
					sj.add(String.format("%,d", i));
				}
			}
			
			if (sj.length() > 0 && LOG.isInfoEnabled()) {
				LOG.info("{}: {} rows were affected in {}", getTable(), sj, LogUtil.formatDuration(start, System.currentTimeMillis()));
			}
		} catch (SQLException e) {
			LOG.error("Execution failed: {}", e.getMessage());
		}
	}
}
