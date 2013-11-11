/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.renderer;

import org.semanticweb.ontop.model.OBDAQuery;

/**
 * A utility class to render a Source Query object into its representational
 * string.
 */
public class SourceQueryRenderer {
	
	/**
	 * Transforms the given <code>OBDAQuery</code> into a string.
	 */
	public static String encode(OBDAQuery input) {
		if (input == null) {
			return "";
		}
		return input.toString();
	}
	
	private SourceQueryRenderer() {
		// Prevent initialization
	}
}
