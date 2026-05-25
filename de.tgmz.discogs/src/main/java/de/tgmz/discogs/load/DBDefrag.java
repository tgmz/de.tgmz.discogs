/*********************************************************************
* Copyright (c) 10.08.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.logging.LogUtil;

public class DBDefrag {
	private static final Logger LOG = LoggerFactory.getLogger(DBDefrag.class);
	
	public void run() {
		long start = System.currentTimeMillis();
		
		String url = System.getProperty("jakarta.persistence.jdbc.url");
		
		if (url.startsWith("jdbc:h2:file")) {
			try (Connection conn = DriverManager.getConnection(url
						, System.getProperty("jakarta.persistence.jdbc.user")
						, System.getProperty("jakarta.persistence.jdbc.password"));
					CallableStatement defrag = conn.prepareCall("SHUTDOWN DEFRAG")) {
    			LOG.info("Begin database defrag");
    			
    			defrag.execute();
    	        
    	        if (LOG.isInfoEnabled()) {
    	        	LOG.info("Defrag took {}", LogUtil.formatDuration(start, System.currentTimeMillis()));
    	        }
			} catch (SQLException e) {
				LOG.error("Execution failed", e);
			}
		}
	}
}
