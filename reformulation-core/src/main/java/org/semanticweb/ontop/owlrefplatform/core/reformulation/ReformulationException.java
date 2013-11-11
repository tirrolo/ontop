/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.reformulation;

import org.semanticweb.ontop.model.OBDAException;

public class ReformulationException extends OBDAException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3919418976375110086L;

	public ReformulationException(Exception cause) {
		super(cause);
	}

	public ReformulationException(String cause) {
		super(cause);
	}

	public ReformulationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
