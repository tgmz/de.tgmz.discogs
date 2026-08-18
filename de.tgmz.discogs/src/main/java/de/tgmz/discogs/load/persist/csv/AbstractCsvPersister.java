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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.DdlFactory;
import de.tgmz.discogs.load.persist.IPersistable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public abstract class AbstractCsvPersister<T> implements IPersistable<T> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvPersister.class);
	protected Map<Table, DiscogsCsvPrinter> pm;

	protected AbstractCsvPersister(String target, Table... tables) {
		pm = new TreeMap<>();
		
		for (Table table : tables) {
			String s = DdlFactory.getInstance().getDdl(l -> Strings.CI.startsWith(l, String.format("create table %s ", table))).getFirst();

			try {
				DiscogsCsvPrinter csvp = new DiscogsCsvPrinter(target, table);

				List<ColumnDefinition> cds = ((CreateTable) CCJSqlParserUtil.parse(s)).getColumnDefinitions();

				int i = 0;
				Object[] cols = new String[cds.size()];
				
				for (ColumnDefinition cd : cds) {
					cols[i++] = cd.getColumnName();
				}
					
				csvp.printRecord(cols);
					
				pm.put(table, csvp);
			} catch (JSQLParserException | IOException e) {
				LOG.warn("Cannot get column list", e);
			}
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
