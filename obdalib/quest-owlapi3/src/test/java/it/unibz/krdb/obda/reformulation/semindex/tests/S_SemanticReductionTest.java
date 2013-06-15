package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexEngineImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.S_SigmaTBoxOptimizer;

import java.util.List;

import junit.framework.TestCase;

public class S_SemanticReductionTest extends TestCase {
	S_SemanticIndexHelper	helper	= new S_SemanticIndexHelper();

	public void test_2_0_0() throws Exception {
		Ontology ontology = helper.load_onto("test_2_0_0");
		TBoxReasonerImpl reasonerIsa= new TBoxReasonerImpl(ontology,false);
//		DAG isa = DAGConstructor.getISADAG(ontology);
//		isa.index();
		SemanticIndexEngineImpl engine= new SemanticIndexEngineImpl(reasonerIsa);
		S_SigmaTBoxOptimizer reduction = new S_SigmaTBoxOptimizer(ontology, reasonerIsa.getSigmaOntology());
		List<Axiom> rv = reduction.reduce();
		assertEquals(0, rv.size());
	}

	public void test_2_0_1() throws Exception {
		Ontology ontology = helper.load_onto("test_2_0_1");
		TBoxReasonerImpl reasonerIsa= new TBoxReasonerImpl(ontology,false);
//		DAG isa = DAGConstructor.getISADAG(ontology);
//		isa.index();
		SemanticIndexEngineImpl engine= new SemanticIndexEngineImpl(reasonerIsa);
		S_SigmaTBoxOptimizer reduction = new S_SigmaTBoxOptimizer(ontology, reasonerIsa.getSigmaOntology());
		List<Axiom> rv = reduction.reduce();
		assertEquals(0, rv.size());
	}

	public void test_2_1_0() throws Exception {
		Ontology ontology = helper.load_onto("test_2_1_0");
//		DAG isa = DAGConstructor.getISADAG(ontology);
//		isa.index();
		TBoxReasonerImpl reasonerIsa= new TBoxReasonerImpl(ontology,false);
		SemanticIndexEngineImpl engine= new SemanticIndexEngineImpl(reasonerIsa);
		S_SigmaTBoxOptimizer reduction = new S_SigmaTBoxOptimizer(ontology,reasonerIsa.getSigmaOntology());
		List<Axiom> rv = reduction.reduce();
		assertEquals(1, rv.size());
	}

	public void test_1_2_0() throws Exception {
		Ontology ontology = helper.load_onto("test_1_2_0");
//		DAG isa = DAGConstructor.getISADAG(ontology);
//		isa.index();
		TBoxReasonerImpl reasonerIsa= new TBoxReasonerImpl(ontology,false);
		SemanticIndexEngineImpl engine= new SemanticIndexEngineImpl(reasonerIsa);
		S_SigmaTBoxOptimizer reduction = new S_SigmaTBoxOptimizer(ontology, reasonerIsa.getSigmaOntology());
		List<Axiom> rv = reduction.reduce();
		assertEquals(0, rv.size());
	}

	public void test_equivalence() throws Exception {

		/*
		 * The ontology contains A1 = A2 = A3, B1 ISA A1, B1 = B2 = B3, this
		 * gives 9 inferences and R1 = R2 = R3, S1 ISA R1, S1 = S2 = S3, this
		 * gives 36 inferences (counting inverse related inferences, and exist
		 * related inferences. Total, 45 inferences
		 */

		Ontology ontology = helper.load_onto("equivalence-test");
		TBoxReasonerImpl reasonerIsa= new TBoxReasonerImpl(ontology,false);
		DAGImpl dag= reasonerIsa.getDAG();
		SemanticIndexEngineImpl engine= new SemanticIndexEngineImpl(reasonerIsa);
		S_SigmaTBoxOptimizer reduction = new S_SigmaTBoxOptimizer(ontology,OntologyFactoryImpl.getInstance().createOntology());
		List<Axiom> rv = reduction.reduce();
		System.out.println(rv);
		assertEquals(45, rv.size());
	}
	
	
}
