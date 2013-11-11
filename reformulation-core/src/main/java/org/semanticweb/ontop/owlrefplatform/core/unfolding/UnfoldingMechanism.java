/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import java.io.Serializable;

import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.Predicate;

/**
 * This interface should be implemented by any class which implements an
 * unfolding Mechanism which should be integrated into a technique wrapper
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public interface UnfoldingMechanism extends Serializable {

	/**
	 * unfolds the the given datalog program
	 * 
	 * @param query
	 *            the query
	 * @return the unfolded query
	 * @throws Exception
	 */
	public DatalogProgram unfold(DatalogProgram query, String targetPredicate)
			throws OBDAException;

	
}
