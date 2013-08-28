/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.ResultSet;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.SesameConstructTemplate;

import java.util.List;

public interface GraphResultSet extends ResultSet {

	public boolean hasNext() throws OBDAException;

	public List<Assertion> next() throws OBDAException;

	public void close() throws OBDAException;

	TupleResultSet getTupleResultSet();

	void addNewResultSet(List<Assertion> result);
	
	SesameConstructTemplate getTemplate();
	
}
