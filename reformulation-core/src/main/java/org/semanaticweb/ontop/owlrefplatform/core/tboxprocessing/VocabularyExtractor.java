package org.semanaticweb.ontop.owlrefplatform.core.tboxprocessing;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashSet;
import java.util.Set;

import org.semanaticweb.ontop.model.Predicate;
import org.semanaticweb.ontop.ontology.Axiom;
import org.semanaticweb.ontop.ontology.Description;
import org.semanaticweb.ontop.ontology.OClass;
import org.semanaticweb.ontop.ontology.Ontology;
import org.semanaticweb.ontop.ontology.Property;
import org.semanaticweb.ontop.ontology.PropertySomeRestriction;
import org.semanaticweb.ontop.ontology.SubDescriptionAxiom;

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
