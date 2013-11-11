/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.sesame.wrapper;

import java.util.Iterator;

import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlrefplatform.core.abox.QuestMaterializer;
import org.semanticweb.ontop.sesame.SesameStatementIterator;

public class SesameMaterializer {
	
		private Iterator<Assertion> assertions = null;
		private QuestMaterializer materializer;
		
		public SesameMaterializer(OBDAModel model) throws Exception {
			this(model, null);
		}
		
		public SesameMaterializer(OBDAModel model, Ontology onto) throws Exception {
			 materializer = new QuestMaterializer(model, onto);
			 assertions = materializer.getAssertionIterator();
		}
		
		public SesameStatementIterator getIterator() {
			return new SesameStatementIterator(assertions);
		}
		
		public void disconnect() {
			materializer.disconnect();
		}
		
		public long getTriplesCount()
		{ try {
			return materializer.getTriplesCount();
		} catch (Exception e) {
			e.printStackTrace();
		}return -1;
		}
	
		public int getVocabularySize() {
			return materializer.getVocabSize();
		}
}
