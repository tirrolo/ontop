/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.core.resultset;

import java.net.URI;
import java.util.List;

import org.semanticweb.ontop.model.BNode;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAStatement;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.model.ValueConstant;

//import com.hp.hpl.jena.iri.IRI;

public class EmptyQueryResultSet implements TupleResultSet {

	List<String> head = null;
	private OBDAStatement st;

	public EmptyQueryResultSet(List<String> headvariables, OBDAStatement st) {
		this.head = headvariables;
		this.st = st;
	}

	@Override
	public void close() throws OBDAException {
	}

//	@Override
//	public double getDouble(int column) throws OBDAException {
//		return 0;
//	}
//
//	@Override
//	public int getInt(int column) throws OBDAException {
//		return 0;
//	}
//
//	@Override
//	public Object getObject(int column) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public String getString(int column) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public URI getURI(int column) throws OBDAException {
//		return null;
//	}
//	
//	@Override
//	public IRI getIRI(int column) throws OBDAException {
//		return null;
//	}

	@Override
	public int getColumCount() throws OBDAException {
		return head.size();
	}

	@Override
	public int getFetchSize() throws OBDAException {
		return 0;
	}

	@Override
	public List<String> getSignature() throws OBDAException {
		return head;
	}

	@Override
	public boolean nextRow() throws OBDAException {
		return false;
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) throws OBDAException {
		return null;
	}

//	@Override
//	public ValueConstant getLiteral(int column) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public BNode getBNode(int column) throws OBDAException {
//		return null;
//	}
//
	@Override
	public Constant getConstant(String name) throws OBDAException {
		return null;
	}
//
//	@Override
//	public URI getURI(String name) throws OBDAException {
//		return null;
//	}
//	
//	@Override
//	public IRI getIRI(String name) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public ValueConstant getLiteral(String name) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public BNode getBNode(String name) throws OBDAException {
//		return null;
//	}

}
