package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphDAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDescriptionDAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_Equivalences_TestNewDAG extends TestCase{
	
	ArrayList<String> input= new ArrayList<String>();
	ArrayList<String> output= new ArrayList<String>();
	
	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);
	
	public S_Equivalences_TestNewDAG(String name){
		super(name);
	}
	
	public void setUp(){
		
		/** C = B -> ER -> A*/
		input.add("src/test/resources/test/newDag/equivalents1.owl");
		/** B -> A -> ER=C */
		input.add("src/test/resources/test/newDag/equivalents2.owl");
		/** C->B = ER -> A*/
		input.add("src/test/resources/test/newDag/equivalents3.owl");
		/** ER-> A=B=C */
		input.add("src/test/resources/test/newDag/equivalents4.owl");
		/** C=ER=A->B */
		input.add("src/test/resources/test/newDag/equivalents5.owl");
		/** D-> ER=C=B -> A*/
		input.add("src/test/resources/test/newDag/equivalents6.owl");
		/** P-> ER=B -> A  C=L ->ES-> ER */
		input.add("src/test/resources/test/newDag/equivalents7.owl");
		/** B->A=ET->ER C->ES=D->A*/
		input.add("src/test/resources/test/newDag/equivalents8.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents1.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents2.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents3.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents4.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents5.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents6.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents7.owl");
		input.add("src/test/resources/test/newDag/inverseEquivalents8.owl");
		
		
		
	}
	
	public void testEquivalences() throws Exception{
		//for each file in the input
				for (int i=0; i<input.size(); i++){
					String fileInput=input.get(i);
//					String fileOutput=output.get(i);
				
				GraphImpl graph1= InputOWL.createGraph(fileInput);
//				DAGImpl dag2= InputOWL.createDAG(fileOutput);
				
				//transform in a named graph
				GraphDAGImpl transform= new GraphDAGImpl(graph1);
				DAGImpl dag2= transform.getDAG();
				log.debug("Input number {}", i+1 );
				log.info("First graph {}", graph1);
				log.info("Second dag {}", dag2);
				
				assertTrue(testDescendants(graph1,dag2,false));
				assertTrue(testDescendants(dag2,graph1,false));
				}
		
	}
	
	private boolean testDescendants(GraphImpl d1, DAGImpl d2, boolean named){
		boolean result = false;
		TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
		TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
		
		for(Description vertex: d1.vertexSet()){
			if(named){
			
				if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
				Set<Set<Description>> setd1	=reasonerd1.getDescendants(vertex, named);
				Set<Set<Description>> setd2	=reasonerd2.getDescendants(vertex, named);
				result= setd1.containsAll(setd2)& setd2.containsAll(setd1);
			
				}
			}
				else{
					Set<Set<Description>> setd1	=reasonerd1.getDescendants(vertex, named);
//					System.out.println("vertex : "+vertex + " "+setd1);
					Set<Set<Description>> setd2	=reasonerd2.getDescendants(vertex, named);
//					System.out.println("vertex : "+vertex + " "+setd2);
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd1.iterator();
					while (it1.hasNext()) {
					set1.addAll(it1.next());	
						}
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd2.iterator();
					while (it2.hasNext()) {
					set2.addAll(it2.next());	
						}
					result=set1.equals(set2);
					}
			
	
			if(!result)
				break;
		}

		return result;
		
	}

	private boolean testDescendants( DAGImpl d1, GraphImpl d2, boolean named){
		boolean result = false;
		TBoxReasonerImpl reasonerd1= new TBoxReasonerImpl(d1);
		TBoxReasonerImpl reasonerd2= new TBoxReasonerImpl(d2);
		
		for(Description vertex: d1.vertexSet()){
			if(named){
			
				if(d1.getRoles().contains(vertex)| d1.getClasses().contains(vertex)){
				Set<Set<Description>> setd1	=reasonerd1.getDescendants(vertex, named);
				Set<Set<Description>> setd2	=reasonerd2.getDescendants(vertex, named);
				result= setd1.containsAll(setd2)& setd2.containsAll(setd1);
			
				}
			}
				else{
					Set<Set<Description>> setd2	=reasonerd2.getDescendants(vertex, named);
					System.out.println("vertex : "+vertex + " "+setd2);
					Set<Set<Description>> setd1	=reasonerd1.getDescendants(vertex, named);
					System.out.println("vertex : "+vertex + " "+setd1);
					Set<Description> set2 = new HashSet<Description>();
					Iterator<Set<Description>> it1 =setd2.iterator();
					while (it1.hasNext()) {
					set2.addAll(it1.next());	
						}
					Set<Description> set1 = new HashSet<Description>();
					Iterator<Set<Description>> it2 =setd1.iterator();
					while (it2.hasNext()) {
					set1.addAll(it2.next());	
						}
					result=set2.equals(set1);
					}
			
	
			if(!result)
				break;
		}

		return result;
		
	}

}
