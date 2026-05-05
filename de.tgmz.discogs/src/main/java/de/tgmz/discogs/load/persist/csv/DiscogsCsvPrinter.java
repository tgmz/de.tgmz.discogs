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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscogsCsvPrinter {
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsCsvPrinter.class);
	private Set<Number> cache;
	private CSVPrinter p;
	
	public DiscogsCsvPrinter(String target, String table) throws IOException {
		super();
		
		cache = new UnlimitedNumberSet();
		
		p = new CSVPrinter(new BufferedWriter(new FileWriter(target + File.separator + table +".csv", StandardCharsets.UTF_8)), CSVFormat.POSTGRESQL_CSV);
	}
	
	public void printRecordUsingCache(Object... values) throws IOException {
		if (!(values[0] instanceof Number n)	//Failsafe 
				|| cache.add(n)) {
			p.printRecord(values);
		}
	}
	
	public void printRecord(Object... values) throws IOException {
		p.printRecord(values);
	}
	
	public void flush() throws IOException {
		p.flush();
	}

	public void close() throws IOException {
		p.close();
	}
}
