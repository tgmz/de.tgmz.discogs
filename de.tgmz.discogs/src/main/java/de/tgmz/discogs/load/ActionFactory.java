/*********************************************************************
* Copyright (c) 26.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.persist.csv.ITable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
public final class ActionFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ActionFactory.class);
	
	private static final ActionFactory INSTANCE = new ActionFactory();
	
	private Properties prop;
	private ITable[] tables;
	private String root;

	/**
	 * Private constructor for security reasons
	 */
	private ActionFactory() {
		tables = new ITable[0];
	}
	
	public ActionFactory forRoot(String root) {
		this.root = root;
		
		return this;
	}

	public ActionFactory forTables(ITable... tables) {
		this.tables = tables;
		
		return this;
	}

	public static ActionFactory getInstance() {
		return INSTANCE;
	}

	public List<DatabaseAction> create(Action a) {
		switch (a) {
		case LOAD, LOAD_NO_PK, RECONCILE:
			return create(a, Mode.PARALLEL);
		case CONSTRAINT, INDEX, PRIMARY_KEY:
		default:
			return create(a, Mode.SEQUENTIAL);
		}
	}
	
	public List<DatabaseAction> create(Action a, Mode m) {
		List<DatabaseAction> result = new LinkedList<>();
		
		Stream.of(tables).forEach(t -> result.add(new DatabaseAction(t, createSql(t, a))));
		
		result.removeIf( da -> da.getSqls().isEmpty());
		
		switch (m) {
		case DEPENDING: 
			for (DatabaseAction da : result) {
				List<ITable> dependsOn = da.getTable().dependsOn();
				
				da.getPredecessors().addAll(result.stream().filter(da0 -> dependsOn.contains(da0.getTable())).toList());
			}
			
			break;
		case SEQUENTIAL:
			for (int i = 1; i < result.size(); ++i) {
				result.get(i).getPredecessors().add(result.get(i-1));
			}
			
			break;
		case PARALLEL:
		default:
			break;
		}
		
		return result;
	}
	private List<Entry<Object, Object>> getProperties(String keyPattern) {
		if (prop == null) {
			prop = new Properties();
		
			try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("table.properties")) {
				prop.load(is);
			} catch (IOException e) {
				LOG.error("Cannot get table properties", e);
			}
		}
		
		// Contruct an entirely new list so we can sort it.
		return new ArrayList<>(prop.entrySet().stream().filter(e -> ((String) e.getKey()).matches(keyPattern)).toList());
	}
	
	private List<String> createSql(ITable t, Action a) {
		List<String> stmts;
		
		switch (a) {
		case INDEX:
			stmts = getIndex(t);
			
			break;
		case CONSTRAINT:
			stmts = getConstraint(t);
			
			break;
		case PRIMARY_KEY:
			stmts = getPrimaryKey(t);
			
			break;
		case RECONCILE:
			stmts = new LinkedList<>();

			List<Entry<Object, Object>> l = getProperties("^" + t + "\\.?\\d?$");

			Collections.sort(l, (e0,e1) -> ((String) e0.getKey()).compareTo((String) e1.getKey()));

			l.forEach(e -> stmts.add((String) e.getValue()));
			
			break;
		case LOAD, LOAD_NO_PK:
		default:
			boolean noPk = (a == Action.LOAD_NO_PK);
			stmts = new LinkedList<>();

			stmts.add(String.format("DROP TABLE IF EXISTS %s CASCADE", t));
			
			if (System.getProperty("jakarta.persistence.jdbc.url")
					.toLowerCase(Locale.getDefault())
					.startsWith("jdbc:postgresql")) {
				stmts.add(getCreate(t, noPk));
				stmts.add("COMMIT");	// Keep the table even if LOAD fails. Usefull if we want to load only a portion of the database
				stmts.add(String.format("COPY %s FROM '%s' (FORMAT csv, HEADER)", t, String.format("%s/%s.csv", root, t)));
			} else {
				File csv = new File(String.format("%s/%s.csv", root, t));
				
				if (csv.exists()) {
					stmts.add(String.format("%s AS SELECT %s FROM CSVREAD('%s')", getCreate(t, noPk), getColumnList(t), csv.toString()));
				} else {
					stmts.add(getCreate(t, noPk));
				}
			}
			
			stmts.addAll(getInit(t));
			
			break;
		}
		
		return stmts;
	}

	private List<String> getIndex(ITable t) {
		List<String> result = new LinkedList<>();
		
		try {
			for (String s : DdlFactory.getInstance().getDdl(l -> Strings.CI.startsWith(l, "create index"))) {
				CreateIndex ci = (CreateIndex) CCJSqlParserUtil.parse(s);
				
				if (t.toString().equals(ci.getTable().getName())) {
					result.add(s);
				}
			}
		} catch (JSQLParserException e) {
			LOG.warn("Cannot get index", e);
		}
		
		return result;
	}

	private List<String> getConstraint(ITable t) {
		List<String> result = new LinkedList<>();
		
		try {
			for (String s : DdlFactory.getInstance().getDdl(l -> Strings.CI.startsWith(l, "alter table"))) {
				Alter ci = (Alter) CCJSqlParserUtil.parse(s);
				
				if (t.toString().equals(ci.getTable().getName())) {
					result.add(s);
				}
			}
		} catch (JSQLParserException e) {
			LOG.warn("Cannot get alter", e);
		}
		return result;
	}
	
	private String getCreate(ITable t, boolean noPk) {
		String s = DdlFactory.getInstance().getDdl(l -> Strings.CI.startsWith(l, "create table " + t.toString() + " ")).getFirst();
	
		if (noPk) {
			try {
				List<Index> indexes = ((CreateTable) CCJSqlParserUtil.parse(s)).getIndexes();
			
				if (indexes != null) {
					for (Index idx : indexes) {
						if (Strings.CI.containsAny(idx.getType(), "primary key", "unique")) {
							s = Strings.CI.remove(s, ", " + idx.toString());
						}
					}
				}
			} catch (JSQLParserException e) {
				LOG.error("Cannot get primary key", e);
			}
		}
		
		return s;
	}
	
	private List<String> getPrimaryKey(ITable t) {
		List<String> idxs = new LinkedList<>();
		
		try {
			List<Index> indexes = ((CreateTable) CCJSqlParserUtil.parse(getCreate(t, false))).getIndexes();
			
			if (indexes != null) {
				for (Index idx : indexes) {
					if (Strings.CI.containsAny(idx.getType(), "primary key", "unique")) {
						idxs.add(String.format("ALTER TABLE %s ADD %s", t, idx));
					}
				}
			}

		} catch (JSQLParserException e) {
			LOG.error("Cannot get primary key", e);
		}

		return idxs;
	}
	
	private List<String> getInit(ITable t) {
		String t0 = t.toString();
		
		Predicate<String> p0 = l -> Strings.CI.startsWith(l, "update " + t0 + " ");
		Predicate<String> p1 = l -> Strings.CI.startsWith(l, "insert into " + t0 + " ");
		Predicate<String> p2 = l -> Strings.CI.startsWith(l, "insert into " + t0 + "(");
		
		return DdlFactory.getInstance().getDdl(p0.or(p1).or(p2));
	}
	
	private String getColumnList(ITable t) {
		String s = getCreate(t, false);

		try {
			List<ColumnDefinition> cds = ((CreateTable) CCJSqlParserUtil.parse(s)).getColumnDefinitions();
			
			StringJoiner sj = new StringJoiner(",");
			
			cds.forEach(cd -> sj.add(cd.getColumnName()));
			
			return sj.toString();
		} catch (JSQLParserException e) {
			LOG.warn("Cannot get column list, returning default", e);
		}
		
		return "*";
	}
}
