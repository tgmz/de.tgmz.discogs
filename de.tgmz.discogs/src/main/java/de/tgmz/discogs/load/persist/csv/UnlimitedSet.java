/*********************************************************************
* Copyright (c) 05.05.2026 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.csv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.AbstractSet;
import java.util.Iterator;

import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * Unlimited set. Performance of a <code>Set</code> decreases dramatically, if it becomes <i>very</i> large. This implematation
 * avoids this by backonig the <code>Set</code> with a h2 database and poxying it with a first-level-cache.
 */
public class UnlimitedSet<T> extends AbstractSet<T> {
	private static final Logger LOG = LoggerFactory.getLogger(UnlimitedSet.class);
	private static final String SQL_CREATE = "CREATE TABLE %s (id VARCHAR(255) NOT NULL, UNIQUE (id))";
	private static final String SQL_INSERT = "INSERT INTO %s (id) VALUES (?)";
	private Connection con;
	private PreparedStatement pstmt;
	private LoadingCache<T, Boolean> lc;
	
	public UnlimitedSet() {
		super();
		
		try {
			con = DriverManager.getConnection("jdbc:h2:mem:n_cache", "sa", "sa");

			try (Statement stmt = con.createStatement()) {
				String table = "n_cache_" + Instant.now().toEpochMilli();
			
				stmt.executeUpdate(String.format(SQL_CREATE, table));
				
				pstmt = con.prepareStatement(String.format(SQL_INSERT, table));
			}
		} catch (SQLException e) {
			LOG.error("Cannot create, reason:", e);
		}
		
		long l = (long) (Runtime.getRuntime().freeMemory() * 0.0125);
		
		lc = Caffeine
				.newBuilder()
				.maximumSize(l)
				.build(this::insert);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Created first-level-cache with size {}", String.format("%,d", l));
		}
	}
	
	@Override
	public boolean add(T n) {
		boolean b = lc.get(n);
		
		// b == true: n was successfully added to the set/database. Return this success but cache false for future
		// operations so no further INSERT attempts will be made as long as the value resides in the cache
		// b == false: the cache alread contained n: Keep the false value
		if (b) {
			lc.put(n, false);
		}
		
		return b;
	}
	
	private boolean insert(T n) throws SQLException {
		try {
			pstmt.setString(1, n.toString());
			pstmt.executeUpdate();
			
			return true;
		} catch (JdbcSQLIntegrityConstraintViolationException e) {
			return false;
		}
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}
}
