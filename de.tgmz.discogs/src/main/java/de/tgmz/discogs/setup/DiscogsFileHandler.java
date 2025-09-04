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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

import de.tgmz.discogs.domain.Release;
import de.tgmz.discogs.load.ArtistContentHandler;
import de.tgmz.discogs.load.LabelContentHandler;
import de.tgmz.discogs.load.MasterContentHandler;
import de.tgmz.discogs.load.ReleaseContentHandler;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarConsumer;
import me.tongfei.progressbar.ProgressBarStyle;

public class DiscogsFileHandler implements ProgressBarConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(DiscogsFileHandler.class);
	private static Map<String, String> hashes;
	private DiscogsFile df;
	private ProgressBarBuilder pbb;

	public DiscogsFileHandler(DiscogsFile df) {
		super();
		this.df = df;

		pbb = new ProgressBarBuilder()
				.setStyle(ProgressBarStyle.ASCII)
				.hideEta()
				.setConsumer(this);
	}

	public static void main(String[] args) {
		downloadAndImport(args);
		
		System.exit(0);
	}
	
	public static void downloadAndImport(String... args) {
		for (DiscogsFile df : DiscogsFile.values()) {
			if (df.isZipped()) {
				try (DiscogsFileHandler dl = new DiscogsFileHandler(df)) { 
					dl.download();
					
					if (!Boolean.getBoolean("de.tgmz.discogs.skipverification")) {
						dl.verify();
					}
					
					dl.extract();
				} catch (IOException | DiscogsVerificationException e) {
					LOG.error("Error in setup", e);
				}
			}
		}

		Predicate<Release> p = getPredicate(args);
		
		try (InputStream is0 = new FileInputStream(DiscogsFile.ARTISTS.getUnzippedFile());
				InputStream is1 = new FileInputStream(DiscogsFile.LABELS.getUnzippedFile());
				InputStream is2 = new FileInputStream(DiscogsFile.MASTERS.getUnzippedFile());
				InputStream is3 = new FileInputStream(DiscogsFile.RELEASES.getUnzippedFile())) {
			new ArtistContentHandler().run(is0);
			new LabelContentHandler().run(is1);
			new MasterContentHandler().run(is2);
			new ReleaseContentHandler(p).run(is3);
		} catch (IOException e) {
			LOG.error("Cannot setup database, reason", e);
		}
	}

	public void download() throws IOException {
		File gz = df.getKey();

		if (gz.exists()) {
			LOG.info("File {} already present, skipping download", gz);

			return;
		}
		
		LOG.info("Download {} to {}", df.getRemote(), gz);
		
		FileUtils.createParentDirectories(gz);

		HttpURLConnection httpConnection = (HttpURLConnection) (df.getRemote().openConnection());
		long completeFileSize = httpConnection.getContentLengthLong();

		long step = 1_000L;
		
		ProgressBar pb = pbb.setTaskName(df.getFileName()).setInitialMax(completeFileSize / step).build();

		try (BoundedInputStream cis = BoundedInputStream.builder().setInputStream(httpConnection.getInputStream()).get(); 
				OutputStream fos = new FileOutputStream(gz)) {

			pb.setExtraMessage("Downloading...");

			new Thread(() -> {
				try {
					IOUtils.copyLarge(cis, fos);
				} catch (IOException e) {
					if (!Boolean.getBoolean("DISCOGS_TEST")) {	
						LOG.error("Error downloading {}", df.getFileName(), e);
					} else {
						// Ignore this on tests where the files are to small for "copyLarge"
						LOG.debug("Error downloading {}, ignoring", df.getFileName());
					}
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
		File unzipped = df.getUnzippedFile();

		if (unzipped.exists()) {
			LOG.info("File {} already present, skipping extraction", unzipped);

			return;
		}

		LOG.info("Extracting {} to {}", df.getFileName(), unzipped);

		int block = 4 * 1024 * 1024;	// 4 MB

		ProgressBar pb = pbb.setTaskName(df.getUnzippedFileName()).setInitialMax(determineUncompressedSize()).build();

		pb.setExtraMessage("Extracting...");

		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(df.getKey()));
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
		LOG.info("Verifying {}", df.getFileName());
		
		ByteSource byteSource = com.google.common.io.Files.asByteSource(df.getKey());
		HashCode hc = byteSource.hash(Hashing.sha256());
		
		String expected = getHash(StringUtils.substringAfterLast(df.getFileName(), "/"));

		if (!expected.equals(hc.toString())) {
			LOG.error("Expected checksum {} but got {}", expected, hc);
			
			throw new DiscogsVerificationException();
		}
		
		LOG.info("Verification successful");
	}

	private static String getHash(String s) throws IOException {
		if (hashes == null) {
			hashes = new TreeMap<>();

			String crcs = IOUtils.toString(DiscogsFile.CHECKSUM.getRemote(), StandardCharsets.UTF_8);

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
		LOG.info("Finished!");
	}

	/**
	 * Credits: https://stackoverflow.com/users/1943126/michail-alexakis
	 * @return
	 * @throws IOException
	 */
	private long determineUncompressedSize() throws IOException {
		long size = df.getKey().length();
		
		// Based on experience the compression factor is about 5.7
		float estm = 5.7f;
		
		// Let's guess if the uncompressed file is < 4GB
		if (size * estm < Math.pow(1024, 3) * 4) { 
			// This piece of code only works, if the size of the _uncompressed_ file is < 4GB  
			try (RandomAccessFile fp = new RandomAccessFile(df.getKey(), "r")) {
				fp.seek(fp.length() - Integer.BYTES);
				int n = fp.readInt();
				size = Integer.toUnsignedLong(Integer.reverseBytes(n));
				
				LOG.info("Uncompressed size {}", size);
			}
		} else {
			// We simply estimtate the size as "5.6 multiplied compressed size"
			size = (long) (size * estm);
			
			LOG.warn("Cannot determine uncompressed size exactly. Using estimated value of {}", size);
		}
		
		return size;
	}
	
	@SuppressWarnings("unchecked")
	private static Predicate<Release> getPredicate(String... clz) {
		Predicate<Release> p = x -> true;
		
		if (clz != null) {
			try {
				for (String s : clz) {
					p = p.and((Predicate<Release>) Class.forName(s).getDeclaredConstructor().newInstance());
				}
			} catch (ReflectiveOperationException e) {
				LOG.error("Error in predicate setup", e);
			}
		}
		
		return p;
	}
}
