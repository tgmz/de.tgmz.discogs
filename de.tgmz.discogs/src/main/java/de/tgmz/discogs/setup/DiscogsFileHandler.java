/*********************************************************************
* Copyright (c) 17.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarConsumer;
import me.tongfei.progressbar.ProgressBarStyle;

public class DiscogsFileHandler implements ProgressBarConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsFileHandler.class);
	private static Map<String, String> hashes;
	private DiscogsFile df;
	private ProgressBarBuilder pbb;

	public static void main(String[] args) {
		for (DiscogsFile df : DiscogsFile.values()) {
			try (DiscogsFileHandler dl = new DiscogsFileHandler(df)) { 
				dl.download();
				dl.verify();
				dl.extract();
			} catch (IOException | DiscogsVerificationException e) {
				LOG.error("Error in setup", e);
			}
		}
	}
	public DiscogsFileHandler(DiscogsFile df) {
		super();
		this.df = df;

		pbb = new ProgressBarBuilder()
				.setStyle(ProgressBarStyle.ASCII)
				.hideEta()
				.setConsumer(this);
	}

	public void download() throws IOException {
		File gz = df.getZipFile();

		if (gz.exists()) {
			LOG.info("File {} already present, skipping download", gz);

			return;
		}
		
		LOG.info("Download {} to {}", df.getFileName(), gz);

		URL url = URI.create(DiscogsFile.getBaseUrl() + df.getZipFileName()).toURL();
		
		HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
		long completeFileSize = httpConnection.getContentLengthLong();

		long step = 1_000L;
		
		ProgressBar pb = pbb.setTaskName(df.getZipFileName()).setInitialMax(completeFileSize / step).build();

		try (BoundedInputStream cis = BoundedInputStream.builder().setInputStream(httpConnection.getInputStream()).get(); 
				OutputStream fos = new FileOutputStream(gz)) {

			pb.setExtraMessage("Downloading...");

			new Thread(() -> {
				try {
					IOUtils.copyLarge(cis, fos);
				} catch (IOException e) {
					LOG.error("Error downloading {}", df.getZipFileName(), e);
				}
			}).start();

			while (cis.getCount() < completeFileSize) {
				pb.stepTo(Math.floorDiv(cis.getCount(), step));
			}

			pb.stepTo(Math.floorDiv(cis.getCount(), step));
		}
		
		pb.close();
	}

	public void extract() throws IOException {
		File unzipped = df.getFile();

		if (unzipped.exists()) {
			LOG.info("File {} already present, skipping extraction", unzipped);

			return;
		}

		LOG.info("Extracting {} to {}", df.getZipFileName(), unzipped);

		int block = 4 * 1024 * 1024;	// 4 MB

		ProgressBar pb = pbb.setTaskName(df.getFileName()).setInitialMax(determineUncompressedSize()).build();

		pb.setExtraMessage("Extracting...");

		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(df.getZipFile()));
				FileOutputStream fos = new FileOutputStream(unzipped)) {
			long size = 0L;

			byte[] buffer = new byte[block];
			int len;
			while ((len = gis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);

				size += len;

				pb.stepTo(size);
			}
		}
		
		pb.close();
	}

	public void verify() throws IOException,DiscogsVerificationException {
		LOG.info("Verifying {}", df.getZipFileName());
		
		ByteSource byteSource = com.google.common.io.Files.asByteSource(df.getZipFile());
		HashCode hc = byteSource.hash(Hashing.sha256());
		
		String expected = getHash(df.getZipFileName());

		if (!getHash(df.getZipFileName()).equals(hc.toString())) {
			LOG.error("Expected checksum {} but got {}", expected, hc);
			
			throw new DiscogsVerificationException();
		}
	}

	private static String getHash(String s) throws IOException {
		if (hashes == null) {
			hashes = new TreeMap<>();

			String crcs = IOUtils.toString(URI.create(DiscogsFile.getBaseUrl() + DiscogsFile.getKey() + "CHECKSUM.txt"), StandardCharsets.UTF_8);

			crcs.lines().forEach(x -> hashes.put(x.substring(65), x.substring(0, 64)));
		}

		return hashes.get(s);
	}

	@Override
	public int getMaxRenderedLength() {
		return 120;
	}

	@Override
	public void accept(String rendered) {
		LOG.info("{}", rendered);
	}

	@Override
	public void close() {
		LOG.info("Success!");
	}

	/**
	 * Credits: https://stackoverflow.com/users/1943126/michail-alexakis
	 * @return
	 * @throws IOException
	 */
	private long determineUncompressedSize() throws IOException {
		long size = df.getZipFile().length();
		
		// Based on experience the compression factor is about 5.6
		float estm = 5.6f;
		
		// Let's guess if the uncompressed file is < 4GB
		if (size * estm < Math.pow(1024, 3) * 4) { 
			// This piece of code only works, if the size of the _uncompressed_ file is < 4GB  
			try (RandomAccessFile fp = new RandomAccessFile(df.getZipFile(), "r")) {
				fp.seek(fp.length() - Integer.BYTES);
				int n = fp.readInt();
				size = Integer.toUnsignedLong(Integer.reverseBytes(n));
			}
		} else {
			// We simply estimtate the size as "5.6 multiplied compressed size"
			size = (long) (size * estm);
			
			LOG.warn("Cannot determine uncompressed size exactly. Using estimated value of {}", size);
		}
		
		return size;
	}
}
