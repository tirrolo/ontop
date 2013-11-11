/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.utils;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;

public class QueryUtils {

	public static void copyQueryModifiers(OBDAQuery source, OBDAQuery target) {
		target.getQueryModifiers().copy(source.getQueryModifiers());
	}

	public static boolean isBoolean(DatalogProgram query) {
		for (CQIE rule : query.getRules()) {
			if (!isBoolean(rule))
				return false;
		}
		return true;
	}

	public static boolean isBoolean(CQIE query) {
		return query.getHead().getArity() == 0;
	}

	public static boolean isGrounded(Term term) {
		boolean result = true;
		if (term instanceof Variable) {
			result = false;
		} else if (term instanceof Function) {
			Function func = (Function) term;
			for (Term subTerm : func.getTerms()) {
				if (!isGrounded(subTerm))
					result = false;
			}
		} 
		return result;
	}
}
