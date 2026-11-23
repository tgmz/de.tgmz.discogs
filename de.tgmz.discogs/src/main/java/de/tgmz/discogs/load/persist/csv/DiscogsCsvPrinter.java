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
import java.util.LinkedList;
import java.util.List;
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
	private List<String[]> buffer;
	private int threshold;
	
	public DiscogsCsvPrinter(String target, Table table, int threshold) throws IOException {
		super();
		
		this.threshold = threshold;
		
		if (table.getCacheSize() > -1) {
			cache = new UnlimitedSet<>(table.getCacheSize());
		}
		
		Writer w = new BufferedWriter(new FileWriter(target + File.separator + table.toString() + ".csv", StandardCharsets.UTF_8));
		
		p = new CSVWriterBuilder(w).build();
		
		buffer = new LinkedList<>();
	}
	
	public void printRecordUsingCache(Object... values) {
		if (cache.add(String.valueOf(values[0]))) {
			printRecord(values);
		}
	}
	
	public void printRecord(Object... values) {
		String[] s0 = Arrays.stream(values).map(o -> o == null ? null : String.valueOf(o)).toArray(String[]::new);
		
		buffer.add(s0);
		
		if (buffer.size() >= threshold) {
			writeBuffer();
		}
	}
	
	public void flush() throws IOException {
		writeBuffer();
		
		p.flush();
	}

	public void close() throws IOException {
		this.flush();
		
		if (cache != null) {
			cache.clear();
		}
		
		p.close();
	}
	
	private void writeBuffer() {
		p.writeAll(buffer);
		
		buffer.clear();
	}
}
