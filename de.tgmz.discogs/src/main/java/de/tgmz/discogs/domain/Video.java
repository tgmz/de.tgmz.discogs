/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Transient;

@Entity
public class Video extends AtomicEntity<String> {
	@Transient
	private static final long serialVersionUID = 6442959493920135549L;

	private int duration;
	private boolean embed;
	private String title;
	@Lob
	private String description;
	
	public Video() {
		super();
	}

	public Video(String id) {
		super(id);
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isEmbed() {
		return embed;
	}

	public void setEmbed(boolean embed) {
		this.embed = embed;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}