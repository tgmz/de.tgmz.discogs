/*********************************************************************
* Copyright (c) 21.04.2026 Thomas Zierer
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
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.load.persist.csv.Table;
import de.tgmz.discogs.logging.LogUtil;
import jakarta.persistence.EntityManager;

public class CsvLoader {
	private static final Logger LOG = LoggerFactory.getLogger(CsvLoader.class);
	private String ddl;
	private Properties prop;
	private String root;

	public CsvLoader(String root) {
		super();
		
		this.root = root;
	}
	
	public void load() {
		for (Table t : Table.values()) {
			loadTable(t);
		}
	}
	
	public void loadTable(Table table) {
		List<String> stmts = new LinkedList<>();
		
		stmts.add(String.format("DROP TABLE IF EXISTS %s CASCADE", table));
		
		if (DatabaseService
				.getInstance()
				.getEntityManagerFactory()
				.getProperties()
				.get("jakarta.persistence.jdbc.url")
				.toString()
				.toLowerCase(Locale.getDefault())
				.startsWith("jdbc:postgresql")) {
			stmts.add(getCreate(table));
			
			stmts.add(String.format("COPY %s FROM '%s' (FORMAT csv, HEADER)", table, String.format("%s/%s.csv", root, table)));
		} else {
			File csv = new File(String.format("%s/%s.csv", root, table));
			
			if (csv.exists()) {
				stmts.add(String.format("%s AS SELECT * FROM CSVREAD('%s')", getCreate(table), csv.toString()));
			} else {
				stmts.add(getCreate(table));
			}
		}
		
		stmts.addAll(getInit(table));
		
		Map<String, String> t = new TreeMap<>();

		for (Entry<Object, Object> e : getProperties().entrySet()) {
			if (((String) e.getKey()).matches("^" + table + "\\.?\\d?$")) {
				t.put((String) e.getKey(), (String) e.getValue());
			}
		}

		t.forEach((k,v) -> stmts.add(v));
		
		stmts.addAll(getAlter(table));
		stmts.addAll(getIndex(table));
		
		for (String stmt : stmts) {
			execute(stmt);
		}
	}
	private String getCreate(Table table) {
		return getDdl().lines().filter(l -> l.startsWith("create table " + table.toString() + " ")).findFirst().orElse("");
	}
	
	private List<String> getInit(Table table) {
		List<String> result = new LinkedList<>();
		
		String ddl = getDdl();
		
		result.addAll(ddl.lines().filter(l -> l.startsWith("update " + table.toString() + " ")).toList());
		result.addAll(ddl.lines().filter(l -> l.startsWith("insert into " + table.toString() + " ")).toList());
		result.addAll(ddl.lines().filter(l -> l.startsWith("insert into " + table.toString() + "(")).toList());
		
		return result;
	}
	
	private List<String> getAlter(Table table) {
		return getDdl().lines().filter(l -> l.startsWith("alter table if exists " + table.toString() + " ")).toList();
	}
	
	private List<String> getIndex(Table table) {
		return getDdl().lines().filter(l -> l.matches("^create index \\w+ on " + table.toString() + " .*$")).toList();
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
	private void execute(String sql) {
		long start = System.currentTimeMillis();
		
		LOG.info("Execute {}", sql);

		try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			em.runWithConnection((Connection conn) -> {
				int i = conn.createStatement().executeUpdate(sql);
				
				if (i > 0 && LOG.isInfoEnabled()) {
					LOG.info("{} rows were affected in {}", String.format("%,d", i), LogUtil.formatDuration(start, System.currentTimeMillis()));
    			}
			});
		}
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
}
