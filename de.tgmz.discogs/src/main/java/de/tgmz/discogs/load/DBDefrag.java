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

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.logging.LogUtil;
import jakarta.persistence.EntityManager;

public class DBDefrag {
	private static final Logger LOG = LoggerFactory.getLogger(DBDefrag.class);
	
	public void run() {
        try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
        	em.runWithConnection((Connection conn) -> {
        		if (conn.getMetaData().getURL().startsWith("jdbc:h2:file")) {
        			LOG.info("Begin database defrag");
        			
        			conn.prepareCall("SHUTDOWN DEFRAG").execute();
        			
        			LOG.info("End database defrag");
        		}
        	});
        }
        
        LogUtil.logElapsed();
	}
}
