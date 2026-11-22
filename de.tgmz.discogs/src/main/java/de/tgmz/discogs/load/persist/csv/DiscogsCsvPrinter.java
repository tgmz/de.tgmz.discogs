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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

public class DiscogsCsvPrinter {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsCsvPrinter.class);
	private Set<String> cache;
	private ICSVWriter p;
	
	public DiscogsCsvPrinter(String target, Table table) throws IOException {
		super();
		
		if (table.getCacheSize() > -1) {
			cache = new UnlimitedSet<>(table.getCacheSize());
		}
		
		Writer w = new BufferedWriter(new FileWriter(target + File.separator + table.toString() + ".csv", StandardCharsets.UTF_8));
		
		p = new CSVWriterBuilder(w).build();
	}
	
	public void printRecordUsingCacheWithColumn(int idCol, Object... values) {
		if (cache.add(String.valueOf(values[idCol]))) {
			printRecord(values);
		}
	}
	
	public void printRecordUsingCache(Object... values) {
		printRecordUsingCacheWithColumn(0, values);
	}
	
	public void printRecord(Object... values) {
		String[] s0 = Arrays.stream(values).map(o -> o == null ? null : String.valueOf(o)).toArray(String[]::new);
		
		p.writeNext(s0);
	}
	
	public void flush() throws IOException {
		p.flush();
	}

	public void close() throws IOException {
		if (cache != null) {
			cache.clear();
		}
		
		p.close();
	}
}
