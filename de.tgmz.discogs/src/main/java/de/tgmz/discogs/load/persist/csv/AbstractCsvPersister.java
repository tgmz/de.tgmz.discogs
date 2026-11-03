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
import java.util.function.Predicate;

import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.DdlFactory;
import de.tgmz.discogs.load.persist.IPersister;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public abstract class AbstractCsvPersister<T> implements IPersister<T> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvPersister.class);
	protected Map<Table, DiscogsCsvPrinter> pm;
	private Predicate<T> filter;

	protected AbstractCsvPersister(Predicate<T> filter, Table... tables) {
		this.filter = filter;
		
		pm = new TreeMap<>();
		
		for (Table table : tables) {
			List<String> createDdl = DdlFactory.getInstance().getDdl(l -> Strings.CI.startsWith(l, String.format("create table %s ", table)));
			
			if (createDdl.size() > 1) {
				throw new IllegalStateException("Multiple definitions found for table " + table);
			}

			try {
				DiscogsCsvPrinter csvp = new DiscogsCsvPrinter(System.getProperty("DISCOGS_CSV_TARGET"), table);

				// If size == 0 enforce IndexOutOfBoundsException
				Object[] cols = ((CreateTable) CCJSqlParserUtil.parse(createDdl.getFirst()))
									.getColumnDefinitions()
									.stream()
									.map(cd -> cd.getColumnName())
									.toArray(Object[]::new);
					
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

	protected Predicate<T> getFilter() {
		return filter;
	}
	
	protected abstract int doSave(T l) throws IOException;
}
