/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.renderer;

import java.util.Iterator;
import java.util.List;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;

/**
 * A utility class to render a Datalog Program object into its representational
 * string.
 */
public class DatalogProgramRenderer {

	/**
	 * Transforms the given <code>DatalogProgram</code> into a string
	 */
	public static String encode(DatalogProgram input) {
		List<CQIE> list = input.getRules();
		Iterator<CQIE> it =list.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()){
			CQIE q = it.next();
			if(sb.length()>0){
				sb.append("\n");
			}
			sb.append(q);
		}
		return sb.toString();
	}

	private DatalogProgramRenderer() {
		// Prevent initialization
	}
}
