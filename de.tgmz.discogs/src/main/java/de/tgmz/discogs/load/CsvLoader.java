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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.database.DatabaseService;
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
	
	public void setRoot(String root) {
		this.root = root;
	}
	
	public void load() {
			loadTable("Artist");
			loadTable("artist_aliases_all");
			loadTable("artist_aliases");
			loadTable("artist_groups_all");
			loadTable("artist_groups");
			loadTable("artist_members_all");
			loadTable("artist_members");
			loadTable("Artist_variations");
   		
			loadTable("Label");
   		
			loadTable("Master");
			loadTable("Genre");
			loadTable("Master_Genre");
			loadTable("Style");
			loadTable("Master_Style");
			loadTable("artist_master_all");
			loadTable("Master_Artist");
   		
			loadTable("Series");
			loadTable("Release");
			loadTable("Release_Genre");
			loadTable("Release_Style");
			loadTable("Release_labels");
			loadTable("EntityType");
			loadTable("Company");
			loadTable("release_company");
			loadTable("Format");
			loadTable("Format_descriptions");
			loadTable("Release_Format");
			loadTable("artist_release_all");		
			loadTable("Release_Artist");
			loadTable("ExtraArtist");
 			loadTable("artist_release_extraartist_all");
			loadTable("release_extraartist");
			loadTable("ReleaseExtraArtist_applicableTracks");
			
   			loadTable("Track");
 			loadTable("artist_release_track_all");
 			loadTable("Track_Artist");
			loadTable("artist_release_track_extraartist_all");
			loadTable("Track_ExtraArtist");
			loadTable("Release_Track");
 			loadTable("SubTrack");
			loadTable("artist_release_subtrack_extraartist_all");
			loadTable("SubTrack_ExtraArtist");
			loadTable("Track_SubTrack");
	}
	
	public void loadTable(String table) {
		List<String> stmts = new LinkedList<>();
		
		stmts.add(String.format("DROP TABLE IF EXISTS %s CASCADE", table));
		
		File csv = new File(String.format("%s/%s.csv", root, table));
		
		if (DatabaseService
				.getInstance()
				.getEntityManagerFactory()
				.getProperties()
				.get("jakarta.persistence.jdbc.url")
				.toString()
				.startsWith("jdbc:postgresql")) {
			stmts.add(getCreate(table));
			
			if (csv.exists()) {
				stmts.add(String.format("COPY %s FROM '%s' (FORMAT csv, HEADER)", table, csv.toString()));
			}
		} else {
			if (csv.exists()) {
				stmts.add(String.format("%s AS SELECT * FROM CSVREAD('%s')", getCreate(table), csv.toString()));
			} else {
				stmts.add(getCreate(table));
			}
		}
		
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
	private String getCreate(String table) {
		return getDdl().lines().filter(l -> l.startsWith("create table " + table + " ")).findFirst().orElse("");
	}
	
	private List<String> getAlter(String table) {
		return getDdl().lines().filter(l -> l.startsWith("alter table if exists " + table + " ")).toList();
	}
	
	private List<String> getIndex(String table) {
		return getDdl().lines().filter(l -> l.matches("^create index \\w+ on " + table + " .*$")).toList();
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
					Triple<Long, Long, Long> t = LogUtil.computeTime(start, System.currentTimeMillis());
    				
					LOG.info("{} rows were affected in {} hours, {} minutes, {} seconds", String.format("%,d", i), t.getLeft(), t.getMiddle(), t.getRight());
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
