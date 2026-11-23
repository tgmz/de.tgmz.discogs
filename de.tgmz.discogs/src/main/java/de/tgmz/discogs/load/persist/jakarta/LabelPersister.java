/*********************************************************************
* Copyright (c) 09.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.persist.jakarta;

import java.util.function.Predicate;

import de.tgmz.discogs.database.DatabaseService;
import de.tgmz.discogs.domain.Label;
import de.tgmz.discogs.load.factory.BasicEntityFactory;
import de.tgmz.discogs.load.factory.collections.CollectionFactory;
import jakarta.persistence.EntityManager;

public class LabelPersister extends AbstractJakartaPersistable<Label> {
	private CollectionFactory<Label> lsf;
	
	public LabelPersister(Predicate<Label> filter, int threshold) {
		super(filter, new BasicEntityFactory<>(), threshold);
		
		lsf = new CollectionFactory<>(getFactory());
	}

	@Override
	// Inserting labels in batch mode cause trouble if a label is its own parent label
	public int save(Label label) {
		if (getFilter().test(label)) {
			LOG.debug("Save {}", label);
			
			try (EntityManager em = DatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
				em.getTransaction().begin();
				
				Label l = getFactory().get(em, label);
				
				if (l.getDataQuality() == null) {
					l.setDataQuality(label.getDataQuality());
				}
			
				Label pl = label.getParentLabel();
			
				if (pl != null) {
					if (pl.equals(l)) {
						// Crazy, but happens (label.id = 219423, name=RDM Edition)
						l.setParentLabel(l);
					} else {
						l.setParentLabel(getFactory().get(em, pl));
					}
				}
				
				l.setSubLabels(lsf.replaceAll(em, l.getSubLabels()));
			
				em.merge(l);
				
				em.getTransaction().commit();
			}
			
			return 1;
		} else {
			return 0;
		}
	}
}
