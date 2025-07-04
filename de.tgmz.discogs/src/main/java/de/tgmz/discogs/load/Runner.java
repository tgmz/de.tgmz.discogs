/*********************************************************************
* Copyright (c) 04.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class Runner implements Callable<Integer> {
	private ReleaseContentHandler rch;
	private File file;
	
	public Runner(ReleaseContentHandler rch, File file) {
		this.rch = rch;
		this.file = file;
	}
	
	@Override
	public Integer call() throws FileNotFoundException {
		rch.run(getStream(file));
		
		return 1;
	}
	private static InputStream getStream(File f) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(f));
	}
}
