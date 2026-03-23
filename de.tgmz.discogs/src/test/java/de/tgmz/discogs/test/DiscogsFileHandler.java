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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
	private ProgressBarBuilder pbb;

	public DiscogsFileHandler() {
		super();

		pbb = new ProgressBarBuilder()
				.setStyle(ProgressBarStyle.ASCII)
				.hideEta()
				.setConsumer(this);
	}

	public File extract(File source, Path targetDir) throws IOException {
		File target = new File(targetDir.toFile(), FilenameUtils.removeExtension(source.getName()));
		
		if (target.exists()) {
			LOG.info("File {} already present, skipping extraction", target);

			return target;
		}

		LOG.info("Extracting {} to {}", source, target);

		int block = 4 * 1024 * 1024;	// 4 MiB

		ProgressBar pb = pbb.setTaskName(target.getName()).setInitialMax(determineUncompressedSize(source)).build();

		pb.setExtraMessage("Extracting...");

		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source));
				FileOutputStream fos = new FileOutputStream(target)) {
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
		
		return target;
	}

	public void verify(File source) throws IOException {
		LOG.info("Verifying {}", source);
		
		ByteSource byteSource = com.google.common.io.Files.asByteSource(source);
		HashCode hc = byteSource.hash(Hashing.sha256());
		
		String expected = getHash(source.getName());

		if (!expected.equals(hc.toString())) {
			LOG.error("Expected checksum {} but got {}", expected, hc);
			
			throw new IOException();
		}
		
		LOG.info("Verification successful");
	}

	private static String getHash(String s) throws IOException {
		if (hashes == null) {
			hashes = new TreeMap<>();
			
			try (InputStream is = DiscogsTest.class.getClassLoader().getResourceAsStream("discogs_CHECKSUM.txt")) {
				String crcs = IOUtils.toString(is, StandardCharsets.UTF_8);

				crcs.lines().forEach(x -> hashes.put(x.substring(65), x.substring(0, 64)));
			}
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
		LOG.info("Finished!");
	}

	/**
	 * Credits: https://stackoverflow.com/users/1943126/michail-alexakis
	 * @return
	 * @throws IOException
	 */
	private long determineUncompressedSize(File source) throws IOException {
		long size = Files.size(source.toPath());
		
		// Based on experience the compression factor is about 5.7
		float estm = 5.7f;
		
		// Let's guess if the uncompressed file is < 4GiB
		if (size * estm < Math.pow(1024, 3) * 4) { 
			// This piece of code only works, if the size of the _uncompressed_ file is < 4GB  
			try (RandomAccessFile fp = new RandomAccessFile(source, "r")) {
				fp.seek(fp.length() - Integer.BYTES);
				int n = fp.readInt();
				size = Integer.toUnsignedLong(Integer.reverseBytes(n));
				
				LOG.info("Uncompressed size {}", String.format("%,d", size));
			}
		} else {
			// We simply estimtate the size as "5.7 multiplied by compressed size"
			size = (long) (size * estm);
			
			LOG.warn("Cannot determine uncompressed size exactly. Using estimated value of {}", String.format("%,d", size));
		}
		
		return size;
	}
}
