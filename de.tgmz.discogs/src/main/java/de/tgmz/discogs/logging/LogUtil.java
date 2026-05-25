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
	private static final String FORM = "%d msec%s";
	private static final String FORM_S = "%d second%s, " + FORM;
	private static final String FORM_MS = "%d minute%s, " + FORM_S; 
	private static final String FORM_HMS = "%d hour%s, " + FORM_MS; 
	
	private LogUtil() {
	}
	
	public static void logElapsed() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Elapsed time: {}", formatElapsed());
		}
	}
	public static String formatElapsed() {
		return formatDuration(ManagementFactory.getRuntimeMXBean().getStartTime(), System.currentTimeMillis());
	}
	public static String formatDuration(long start, long end) {
		long msecs = (end - start) % 1000;
		long seconds = (end - start) / 1000;
		long minutes = (seconds / 60) % 60 ;
		long hours = seconds / (60 * 60);
		seconds %= 60;
		
		if (hours > 0) {
			return String.format(FORM_HMS
					, hours, getSuffix(hours)
					, minutes, getSuffix(minutes)
					, seconds, getSuffix(seconds)
					, msecs, getSuffix(msecs)
					);
		}
		
		if (minutes > 0) {
			return String.format(FORM_MS
					, minutes, getSuffix(minutes)
					, seconds, getSuffix(seconds)
					, msecs, getSuffix(msecs)
					);
		}
		
		if (seconds > 0) {
			return String.format(FORM_S
					, seconds, getSuffix(seconds)
					, msecs, getSuffix(msecs)
					);
		}
		
		return String.format(FORM
				, msecs, getSuffix(msecs)
				);
	}
	
	private static String getSuffix(long i) {
		return i == 1 ? "" : "s";
	}
}
