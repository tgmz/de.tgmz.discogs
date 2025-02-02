/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.logging;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
	private static final Logger LOG = LoggerFactory.getLogger(LogUtil.class);
	
	private LogUtil() {
	}
	
	public static void logElapsed() {
		long start = ManagementFactory.getRuntimeMXBean().getStartTime();
		
		if (LOG.isInfoEnabled()) {
			long end = System.currentTimeMillis();
			
			long seconds = (end - start) / 1000;
			long minutes = (seconds / 60) % 60 ;
			long hours = seconds / (60 * 60);
			seconds %= 60; 
			
			LOG.info("Elapsed time: {} hours, {} minutes, {} seconds", hours, minutes, seconds);
		}
	}

}
