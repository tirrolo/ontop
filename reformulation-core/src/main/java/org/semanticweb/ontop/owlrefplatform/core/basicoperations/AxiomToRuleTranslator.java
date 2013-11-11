/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Axiom;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.ontology.SubDescriptionAxiom;

public class AxiomToRuleTranslator {
	
	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		
	public static CQIE translate(Axiom axiom) throws UnsupportedOperationException {
		if (axiom instanceof SubDescriptionAxiom) {
			SubDescriptionAxiom subsumption = (SubDescriptionAxiom) axiom;
			Description descLeft = subsumption.getSub();
			Description descRight = subsumption.getSuper();
			
			Function head = translate(descRight);
			Function body = translate(descLeft);
			
			return ofac.getCQIE(head, body);
		} else {
			throw new UnsupportedOperationException("Unsupported type of axiom: " + axiom.toString());
		}
	}
		
	public static Function translate(Description description) throws UnsupportedOperationException {
		final Variable varX = ofac.getVariable("x");
		final Variable varY = ofac.getVariable("y");
		if (description instanceof OClass) {
			OClass klass = (OClass) description;
			return ofac.getFunction(klass.getPredicate(), varX);
		} else if (description instanceof Property) {
			Property property = (Property) description;
			if (property.isInverse()) {
				return ofac.getFunction(property.getPredicate(), varY, varX);
			} else {
				return ofac.getFunction(property.getPredicate(), varX, varY);
			}
		} else if (description instanceof PropertySomeRestriction) {
			PropertySomeRestriction property = (PropertySomeRestriction) description;
			if (property.isInverse()) {
				return ofac.getFunction(property.getPredicate(), varY, varX);
			} else {
				return ofac.getFunction(property.getPredicate(), varX, varY);
			}
		} else {
			throw new UnsupportedOperationException("Unsupported type of description: " + description.toString());
		}
	}
}
