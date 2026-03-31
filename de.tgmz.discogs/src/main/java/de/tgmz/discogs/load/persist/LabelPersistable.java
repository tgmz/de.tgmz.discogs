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

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.load.factory.AtomicEntityFactory;
import de.tgmz.discogs.load.factory.IFactory;
import jakarta.persistence.EntityManager;

public class LabelPersistable extends AbstractDefaultPersistable<Label> {
	private Predicate<Label> filter;
	private IFactory<Label> lf;
	
	public LabelPersistable() {
		this(x -> true);
	}

	public LabelPersistable(Predicate<Label> filter) {
		this.filter = filter;
		
		lf = new AtomicEntityFactory<>();
	}

	@Override
	// Inserting labels in batch mode cause trouble if a label is its own parent label
	public int save(int threshold, Label label) {
		if (filter.test(label)) {
			try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
				em.getTransaction().begin();
				
				Label l = lf.get(em, label);
				
				if (l.getDataQuality() == null) {
					l.setDataQuality(label.getDataQuality());
				}
			
				Label pl = label.getParentLabel();
			
				if (pl != null) {
					if (pl.equals(l)) {
						// Crazy, but happens (label.id = 219423, name=RDM Edition)
						l.setParentLabel(l);
					} else {
						l.setParentLabel(lf.get(em, pl));
					}
				}
			
				em.merge(l);
				
				em.getTransaction().commit();
			}
			
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public IFactory<Label> getFactory() {
		return null;
	}

	@Override
	public Predicate<Label> getFilter() {
		return filter;
	}
}
