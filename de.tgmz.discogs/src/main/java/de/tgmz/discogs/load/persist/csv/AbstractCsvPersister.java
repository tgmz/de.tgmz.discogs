/*********************************************************************
* Copyright (c) 21.04.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.csv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.load.persist.IPersistable;
import jakarta.persistence.EntityManager;

public abstract class AbstractCsvPersister<T> implements IPersistable<T> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvPersister.class);
	private static final String SQL_COLS = "SELECT * FROM %s";
	protected Map<Table, DiscogsCsvPrinter> pm;

	protected AbstractCsvPersister(String target, Table... tables) {
		pm = new TreeMap<>();
		
		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			em.runWithConnection((Connection con) -> {
				Statement st = con.createStatement();
				
				for (Table table : tables) {
					DiscogsCsvPrinter csvp = new DiscogsCsvPrinter(target, table);
					
					Object[] cols;
					
					try {
						ResultSetMetaData rsmd = st.executeQuery(String.format(SQL_COLS, table)).getMetaData();
					
						cols = new String[rsmd.getColumnCount()];
					
						for (int i = 0; i < rsmd.getColumnCount(); ++i) {
							cols[i] = rsmd.getColumnName(i + 1);
						}
					} catch (SQLException e) {
						cols = new String[] {"ID", "NAME"};
					}
					
					csvp.printRecord(cols);
					
					pm.put(table, csvp);
				}
			});
		}
	}
	
	@Override
	public int save(int threshold, T o) {
		try {
			return doSave(o);
		} catch (IOException e) {
			LOG.error("", e);
		}
		
		return 0;
	}
	
	@Override
	public int flush() {
		try {
			for (DiscogsCsvPrinter e : pm.values()) {
				e.flush();
				e.close();
			}
		} catch (IOException e) {
			LOG.error("", e);
		}
		
		return 0;
	}
	
	protected abstract int doSave(T l) throws IOException;
}
