/*********************************************************************
* Copyright (c) 11.07.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.discogs.load.factory;

import de.tgmz.discogs.domain.Label;
import jakarta.persistence.EntityManager;

public class LabelFactory implements IFactory<Label> {
	private EntityManager em;
	
	@Override
	public Label get(EntityManager em, Label draft) {
		this.em = em;
		
		Label l = findOrCreate(draft);
		
		if (l.getDataQuality() == null) {
			l.setDataQuality(draft.getDataQuality());
		}
		
		return l;
	}
	
	private Label findOrCreate(Label draft) {
		Label a0 = em.find(Label.class, draft.getId());
		
		if (a0  == null) {
			a0 = draft;
			
			if (a0.getParentLabel() == null && draft.getParentLabel() != null) {
				Label pl = draft.getParentLabel();
				
				if (!pl.getId().equals(draft.getId())) {
					a0.setParentLabel(findOrCreate(pl));
				} else {
					// Crazy, but happens (label.id = 219423, name=RDM Edition)
					a0.setParentLabel(a0);
				}
			}
			
			em.persist(a0);
		}
		
		return a0;
	}
}

