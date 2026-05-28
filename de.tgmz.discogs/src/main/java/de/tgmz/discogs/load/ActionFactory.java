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
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.persist.csv.Table;
public final class ActionFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ActionFactory.class);
	private static final ActionFactory INSTANCE = new ActionFactory();
	private String ddl;
	private Properties prop;

	/**
	 * Private constructor for security reasons
	 */
	private ActionFactory() {
	}

	public static ActionFactory getInstance() {
		return INSTANCE;
	}

	public List<PredecessorAwareAction> create(Action a) {
		return create(a, Mode.PARALLEL, null);
	}
	
	public List<PredecessorAwareAction> create(Action a, Mode m) {
		return create(a, m, null);
	}
	
	public List<PredecessorAwareAction> create(Action a, Mode m, String root) {
		List<PredecessorAwareAction> result = new LinkedList<>();
		
		Stream.of(Table.values()).forEach(t -> result.add(new DatabaseAction(t, createSql(t, a, root))));
		
		result.removeIf( da -> ((DatabaseAction) da).getSqls().isEmpty());
		
		switch (m) {
		case DEPENDING: 
			for (PredecessorAwareAction da : result) {
				List<Table> dependsOn = da.getTable().getDependsOn();
				
				da.getPredecessors().addAll(result.stream().filter(la0 -> dependsOn.contains(la0.getTable())).toList());
			}
			
			break;
		case SEQUENTIAL:
			for (int i = 1; i < result.size(); ++i) {
				result.get(i).getPredecessors().add(result.get(i-1));
			}
			
			break;
		case SUMMUP:
			List<String> combinedSql = new LinkedList<>();
			
			result.forEach(pwa -> combinedSql.addAll(((DatabaseAction) pwa).getSqls()));
			
			result.clear();
			result.add(new DatabaseAction(null, combinedSql));
			
			break;
		case PARALLEL:
		default:
			break;
		}
		
		return result;
	}
	private String getDdl() {
		if (ddl == null) {
			try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("discogs.ddl")) {
				ddl = IOUtils.toString(is, StandardCharsets.UTF_8);
			} catch (IOException e) {
				LOG.error("Cannot get DDL", e);
				
				ddl = "";
			}
		}
		
		return ddl;
	}
	private Properties getProperties() {
		if (prop == null) {
			prop = new Properties();
		
			try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("table.properties")) {
				prop.load(is);
			} catch (IOException e) {
				LOG.error("Cannot get table properties", e);
			}
		}
		
		return prop;
	}
	
	private List<String> createSql(Table t, Action a, String root) {
		List<String> stmts;
		
		switch (a) {
		case OPTIMIZE:
			stmts = getDdl().lines().filter(l -> l.matches("^create index \\w+ on " + t.toString() + " .*$")).toList();
			
			break;
		case VALIDATE:
			stmts = getDdl().lines().filter(l -> l.startsWith("alter table if exists " + t.toString() + " ")).toList();
			
			break;
		case RECONCILE:
			stmts = new LinkedList<>();

			Map<String, String> m = new TreeMap<>();

			for (Entry<Object, Object> e : getProperties().entrySet()) {
				if (((String) e.getKey()).matches("^" + t + "\\.?\\d?$")) {
					m.put((String) e.getKey(), (String) e.getValue());
				}
			}

			m.forEach((k,v) -> stmts.add(v));
			
			break;
		case LOAD:
		default:
			stmts = new LinkedList<>();

			stmts.add(String.format("DROP TABLE IF EXISTS %s CASCADE", t));
			
			if (System.getProperty("jakarta.persistence.jdbc.url")
					.toLowerCase(Locale.getDefault())
					.startsWith("jdbc:postgresql")) {
				stmts.add(getCreate(t));
				
				stmts.add(String.format("COPY %s FROM '%s' (FORMAT csv, HEADER)", t, String.format("%s/%s.csv", root, t)));
			} else {
				File csv = new File(String.format("%s/%s.csv", root, t));
				
				if (csv.exists()) {
					stmts.add(String.format("%s AS SELECT * FROM CSVREAD('%s')", getCreate(t), csv.toString()));
				} else {
					stmts.add(getCreate(t));
				}
			}
			
			stmts.addAll(getInit(t));
			
			break;
		}
		
		return stmts;
	}
	
	private String getCreate(Table t) {
		return getDdl().lines().filter(l -> l.startsWith("create table " + t.toString() + " ")).findFirst().orElse("");
	}
	
	private List<String> getInit(Table t) {
		List<String> result = new LinkedList<>();
		
		String ddl0 = getDdl();
		
		result.addAll(ddl0.lines().filter(l -> l.startsWith("update " + t.toString() + " ")).toList());
		result.addAll(ddl0.lines().filter(l -> l.startsWith("insert into " + t.toString() + " ")).toList());
		result.addAll(ddl0.lines().filter(l -> l.startsWith("insert into " + t.toString() + "(")).toList());
		
		return result;
	}
}
