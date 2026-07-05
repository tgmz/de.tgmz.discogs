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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.load.persist.csv.ITable;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
public final class ActionFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ActionFactory.class);
	
	private static final String BASE_PATTERN = "^(create table.*)(,)( %s \\([\\w\\s,]+\\))(.*)$";
	
	// The different types of primary keys used in discogs.ddl
	private static final List<String> PK_TYPE = List.of(
		"constraint \\w+ primary key"	// Named primary key, used with @EmbeddedId
		, "constraint \\w+ unique"		// Named unique, used with @*ToMany when target uses @EmbeddedId
		, "primary key"					// Anonymous primary key, used with @Id
		, "unique"						// Anonymous unique, used with @ElementCollection
	);
	
	private static final Pattern DDL_PATTERN = Pattern.compile("^.*\\.ddl$"); 
		
	private static final ActionFactory INSTANCE = new ActionFactory();
	
	private List<String> ddl;
	private Properties prop;
	private List<Pattern> pkPattern;
	private ITable[] tables;

	/**
	 * Private constructor for security reasons
	 */
	private ActionFactory() {
		pkPattern = new LinkedList<>();
		
		PK_TYPE.forEach(s -> pkPattern.add(Pattern.compile(String.format(BASE_PATTERN, s))));
		
		tables = new ITable[0];
	}
	
	public ActionFactory forTables(ITable[] tables) {
		this.tables = tables;
		
		return this;
	}

	public static ActionFactory getInstance() {
		return INSTANCE;
	}

	public List<DatabaseAction> create(Action a) {
		return create(a, Mode.PARALLEL, null);
	}
	
	public List<DatabaseAction> create(Action a, Mode m) {
		return create(a, m, null);
	}
	
	public List<DatabaseAction> create(Action a, Mode m, String root) {
		List<DatabaseAction> result = new LinkedList<>();
		
		Stream.of(tables).forEach(t -> result.add(new DatabaseAction(t, createSql(t, a, root))));
		
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
		case SUMMUP:
			List<String> combinedSql = new LinkedList<>();
			
			result.forEach(da -> combinedSql.addAll(da.getSqls()));
			
			result.clear();
			result.add(new DatabaseAction(null, combinedSql));
			
			break;
		case PARALLEL:
		default:
			break;
		}
		
		return result;
	}
	private List<String> getDdl() {
		if (ddl == null) {
			Set<String> ddl0 = new TreeSet<>();	// Avoid duplicates lines in case a DDL is multiple times in classpath
			
			try (ScanResult scanResult = new ClassGraph().scan()) {
				for (URI uri : scanResult.getResourcesMatchingPattern(DDL_PATTERN).getURIs()) {
					LOG.info("Loaded DDL from {}", uri);
					
					ddl0.addAll(IOUtils.toString(uri, StandardCharsets.UTF_8).lines().toList());
				}
			} catch (IOException e) {
				LOG.error("Cannot get DDL", e);
			}
			
			ddl = new LinkedList<>(ddl0);
		}
		
		return ddl;
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
	
	private List<String> createSql(ITable t, Action a, String root) {
		List<String> stmts;
		
		switch (a) {
		case INDEX:
			stmts = getDdl().stream().filter(l -> l.matches("^create index \\w+ on " + t.toString() + " .*$")).toList();
			
			break;
		case CONSTRAINT:
			stmts = getDdl().stream().filter(l -> l.startsWith("alter table if exists " + t.toString() + " ")).toList();
			
			break;
		case PRIMARY_KEY:
			stmts = new LinkedList<>();

			Optional.ofNullable(getPrimaryKey(t)).ifPresent(stmts::add);
			
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
					stmts.add(String.format("%s AS SELECT * FROM CSVREAD('%s')", getCreate(t, noPk), csv.toString()));
				} else {
					stmts.add(getCreate(t, noPk));
				}
			}
			
			stmts.addAll(getInit(t));
			
			break;
		}
		
		return stmts;
	}
	
	private String getCreate(ITable t, boolean noPk) {
		String s = getDdl().stream().filter(l -> l.startsWith("create table " + t.toString() + " ")).findFirst().orElseThrow();

		if (noPk) {
			for (Pattern p : pkPattern) {
				Matcher m = p.matcher(s);
		
				if (m.matches()) {
					return m.group(1) + m.group(4);
				}
			}
		}
		
		return s;
	}
	
	private String getPrimaryKey(ITable t) {
		String s = getDdl().stream().filter(l -> l.startsWith("create table " + t.toString() + " ")).findFirst().orElseThrow();

		for (Pattern p : pkPattern) {
			Matcher m = p.matcher(s);
		
			if (m.matches()) {
				return "ALTER TABLE " + t.toString() + " ADD" + m.group(3);
			}
		}
		
		return null;
	}
	
	private List<String> getInit(ITable t) {
		String t0 = t.toString();
		
		return getDdl().stream().filter(l -> l.startsWith("update " + t0 + " ")
										|| l.startsWith("insert into " + t0 + " ")
										|| l.startsWith("insert into " + t0 + "(")).toList();
	}
}
