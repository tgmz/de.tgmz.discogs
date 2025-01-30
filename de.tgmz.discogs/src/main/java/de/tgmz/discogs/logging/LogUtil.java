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
