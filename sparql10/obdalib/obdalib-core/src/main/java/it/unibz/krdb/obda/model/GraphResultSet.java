package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.ontology.Assertion;

import java.util.List;

public interface GraphResultSet {

	public boolean next() throws OBDAException;

	public List<Assertion> getAssertions() throws OBDAException;

	public void close() throws OBDAException;
}