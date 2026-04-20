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

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
	private static final Logger LOG = LoggerFactory.getLogger(LogUtil.class);
	
	private LogUtil() {
	}
	
	public static void logElapsed() {
		long start = ManagementFactory.getRuntimeMXBean().getStartTime();
		
		if (LOG.isInfoEnabled()) {
			Triple<Long, Long, Long> t = computeTime(start, System.currentTimeMillis());
			
			LOG.info("Elapsed time: {} hours, {} minutes, {} seconds", t.getLeft(), t.getMiddle(), t.getRight());
		}
	}
	public static Triple<Long, Long, Long> computeTime(long start, long end) {
		long seconds = (end - start) / 1000;
		long minutes = (seconds / 60) % 60 ;
		long hours = seconds / (60 * 60);
		seconds %= 60;
		
		return Triple.of(hours, minutes, seconds);
	}

}
