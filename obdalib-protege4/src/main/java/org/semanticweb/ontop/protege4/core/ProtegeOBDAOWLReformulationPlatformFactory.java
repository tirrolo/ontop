/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.protege4.core;

import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;

import java.util.Properties;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ProtegeOBDAOWLReformulationPlatformFactory extends AbstractProtegeOWLReasonerInfo {

	QuestOWLFactory factory = new QuestOWLFactory();

	@Override
	public BufferingMode getRecommendedBuffering() {
		return BufferingMode.BUFFERING;
	}

	@Override
	public OWLReasonerFactory getReasonerFactory() {
		return factory;
	}

	public void setPreferences(Properties preferences) {
		factory.setPreferenceHolder(preferences);
	}

	public void setOBDAModel(OBDAModel model) {
		factory.setOBDAController(model);
	}

}
