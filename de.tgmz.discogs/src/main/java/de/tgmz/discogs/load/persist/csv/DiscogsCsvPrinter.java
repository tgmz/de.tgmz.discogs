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
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class DiscogsCsvPrinter {
	private Set<Long> cache;
	private String table;
	private CSVPrinter p;
	
	public DiscogsCsvPrinter(String target, String table) throws IOException {
		super();
		
		this.table = table;
		
		cache = new TreeSet<>();
		
		p = new CSVPrinter(new BufferedWriter(new FileWriter(target + File.separator + table +".csv", StandardCharsets.UTF_8)), CSVFormat.POSTGRESQL_CSV);
	}
	
	public void printRecord(Object... values) throws IOException {
		if (!table.endsWith("_all") 
				|| !(values[0] instanceof Long l)	//Failsafe 
				|| cache.add(l)) {
			p.printRecord(values);
			
			if (cache.size() > 5_000_000) {
				cache.clear();
			}
		}
	}
	public void flush() throws IOException {
		p.flush();
	}
	public void close() throws IOException {
		p.close();
	}
}
