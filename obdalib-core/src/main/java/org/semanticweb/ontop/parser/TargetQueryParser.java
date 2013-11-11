/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.parser;

import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.CQIE;

public interface TargetQueryParser {
	
	public void setPrefixManager(PrefixManager manager);
	
	public CQIE parse(String input) throws TargetQueryParserException;
}
