package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange.Interval;

/** An interface for the class SemanticIndexEngine that build the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {



	public void construct(DAGImpl dag);
	public int getIndex(Description d);
	public Set<Interval> getIntervals(Description d);
}
