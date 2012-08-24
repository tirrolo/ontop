package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.NewLiteral;

public abstract class AbstractTerm implements NewLiteral, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 626920825158789773L;

	@Override
	public abstract NewLiteral clone();

}
