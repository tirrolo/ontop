/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.ontology.impl;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.DataType;

public class DataTypeImpl implements DataType {
	
	private static final long serialVersionUID = -6228610469212615956L;
	
	private Predicate predicate;
	
	public DataTypeImpl(Predicate p) {
		predicate = p;
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return predicate.getName().toString();
	}
}
