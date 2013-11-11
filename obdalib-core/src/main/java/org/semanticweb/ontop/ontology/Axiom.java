/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.ontology;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;

/***
 * A logical axiom.
 */
public interface Axiom  extends Serializable{

	public Set<Predicate> getReferencedEntities();
}
