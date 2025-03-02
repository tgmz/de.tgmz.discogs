/*********************************************************************
* Copyright (c) 17.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.socket.PortFactory;
import org.mockserver.socket.tls.KeyStoreFactory;

import de.tgmz.discogs.logging.LogUtil;
import de.tgmz.discogs.setup.DiscogsFile;
import de.tgmz.discogs.setup.DiscogsFileHandler;
import de.tgmz.discogs.setup.DiscogsVerificationException;

public class SetupTest {
	private static final String LOG_LEVEL_KEY = "org.slf4j.simpleLogger.defaultLogLevel";
	private static ClientAndServer mockServer;
	private static String logLevel;

	@BeforeClass
	public static void setupOnce() throws IOException {
		logLevel = System.getProperty(LOG_LEVEL_KEY, "INFO");
		System.setProperty(LOG_LEVEL_KEY, "INFO"); // Set to "DEBUG" to force noisy logging
		
		System.setProperty(DiscogsFile.DISCOGS_DIR, System.getProperty("java.io.tmpdir"));
		System.setProperty("DISCOGS_TEST", "true");

		HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(Configuration.configuration(), new MockServerLogger()).sslContext().getSocketFactory());

		mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
		
		setupMockRequest("discogs_artists.xml.gz", "/.*artists.*");
		setupMockRequest("discogs_labels.xml.gz", "/.*labels.*");
		setupMockRequest("discogs_masters.xml.gz", "/.*masters.*");
		setupMockRequest("discogs_releases.xml.gz", "/.*releases.*");
		setupMockRequest("discogs_CHECKSUM.txt", "/.*CHECKSUM.*");
		setupMockRequest("root.xml", "/");
		
		System.setProperty(DiscogsFile.DISCOGS_URL, "https://" + mockServer.remoteAddress().getHostName() + ":" + mockServer.remoteAddress().getPort() + "/");
		
		for (DiscogsFile df : DiscogsFile.values()) {
			FileUtils.deleteQuietly(df.getUnzippedFile());
			FileUtils.deleteQuietly(df.getFile());
		}
	}
	
	@AfterClass
	public static void teardownOnce() {
		if (mockServer != null) {
			mockServer.stop();
		}
		
		System.setProperty(LOG_LEVEL_KEY, logLevel);
		
		LogUtil.logElapsed();
	}
	
	@Test
	public void testEverything() throws IOException, DiscogsVerificationException  {
		for (DiscogsFile df : DiscogsFile.values()) {
			if (df.isZipped()) {
				DiscogsFileHandler d = new DiscogsFileHandler(df);

				d.download();
				d.verify();
				d.extract();
		
				d.close();
			}
		}
		
		// Force VerificationException
		mockServer.reset();
		setupMockRequest(new byte[0], "/.*artists.*");
		setupMockRequest("discogs_CHECKSUM.txt", "/.*CHECKSUM.*");
		
		DiscogsFile a = DiscogsFile.ARTISTS;
		assertTrue(a.getFile().delete());
		
		DiscogsFileHandler d = new DiscogsFileHandler(a);

		d.download();
		assertThrows(DiscogsVerificationException.class, d::verify);
		d.close();
	}
	private static void setupMockRequest(String file, String path) throws IOException {
		byte[] b = SetupTest.class.getClassLoader().getResourceAsStream(file).readAllBytes();

		setupMockRequest(b, path);
	}
	private static void setupMockRequest(byte[] b, String path) {
		mockServer.when(HttpRequest.request().withMethod("GET").withPath(path)).respond(HttpResponse.response().withBody(b));
	}
}

