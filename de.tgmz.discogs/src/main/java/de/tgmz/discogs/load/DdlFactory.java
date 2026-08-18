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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
public final class DdlFactory {
	private static final Logger LOG = LoggerFactory.getLogger(DdlFactory.class);
	
	private static final DdlFactory INSTANCE = new DdlFactory();
	
	private List<String> ddl;

	/**
	 * Private constructor for security reasons
	 */
	private DdlFactory() {
		Set<String> ddl0 = new TreeSet<>();	// Avoid duplicates lines in case a DDL is multiple times in classpath
		
		try (ScanResult scanResult = new ClassGraph().scan()) {
			for (URI uri : scanResult.getResourcesMatchingPattern(Pattern.compile("^.*\\.ddl$")).getURIs()) {
				LOG.info("Loaded DDL from {}", uri);
				
				ddl0.addAll(IOUtils.toString(uri, StandardCharsets.UTF_8).lines().toList());
			}
		} catch (IOException e) {
			LOG.error("Cannot get DDL", e);
		}
		
		ddl = new LinkedList<>(ddl0);
	}
	
	public static DdlFactory getInstance() {
		return INSTANCE;
	}

	public List<String> getDdl(Predicate<String> filter) {
		return new LinkedList<>(ddl.stream().filter(filter).toList());
	}
}
