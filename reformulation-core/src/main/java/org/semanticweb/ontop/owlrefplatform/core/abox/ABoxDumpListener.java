/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.abox;

import org.semanticweb.ontop.model.OBDADataSource;

/**
 * A simple listener interface that notifies its classes when a 
 * abox dump was succesfull
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface ABoxDumpListener {

	/**
	 * Notifies that a abox dump was successful
	 * 
	 * @param ds the data source to which the dump was made
	 */
	public void dump_successful(OBDADataSource ds);
}
