/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.Axiom;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.PropertySomeRestriction;
import org.semanticweb.ontop.ontology.SubDescriptionAxiom;

/***
 * Extracts the vocabulary of an ontology.
 * 
 * Warning, it only suports subclass and subproperty axioms and descriptions of
 * the form.
 * 
 * R, R-, A, \exists R, \exists R-
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class VocabularyExtractor {

	public Set<Predicate> getVocabulary(Ontology ontology) {
		Set<Predicate> result = new HashSet<Predicate>();

		for (Axiom axiom : ontology.getAssertions()) {
			if (axiom instanceof SubDescriptionAxiom) {
				SubDescriptionAxiom subClass = (SubDescriptionAxiom) axiom;
				result.add(getPredicate(subClass.getSub()));
				result.add(getPredicate(subClass.getSuper()));
			}
		}

		return result;
	}

	public Predicate getPredicate(Description e) {
		if (e instanceof OClass) {
			return ((OClass) e).getPredicate();
		}
		if (e instanceof PropertySomeRestriction) {
			return ((PropertySomeRestriction) e).getPredicate();
		}
		if (e instanceof Property) {
			return ((Property) e).getPredicate();
		}
		return null;
	}
}
