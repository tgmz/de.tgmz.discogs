/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist;

import java.util.function.Predicate;

import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.load.factory.LabelFactory;
import jakarta.persistence.EntityManager;

public class LabelPersistable implements IPersistable<Label> {
	private Predicate<Label> filter;
	private LabelFactory lf;
	
	public LabelPersistable() {
		this(x -> true);
	}

	public LabelPersistable(Predicate<Label> filter) {
		this.filter = filter;
		
		lf = new LabelFactory();
	}

	@Override
	public int save(EntityManager em, Label label) {
		if (filter.test(label)) {
			Label l = lf.get(label);
			
			Label pl = label.getParentLabel();
			
			if (pl != null) {
				if (pl.getId() != l.getId()) {
					l.setParentLabel(lf.get(pl));
				} else {
					// Crazy, but happens (label.id = 219423, name=RDM Edition)
					l.setParentLabel(l);
				}
			}
			
			em.merge(l);
			
			return 1;
		} else {
			return 0;
		}
	}
}
