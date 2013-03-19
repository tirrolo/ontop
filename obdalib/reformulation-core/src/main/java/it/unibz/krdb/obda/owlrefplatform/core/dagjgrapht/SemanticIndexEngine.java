package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange.Interval;

/** An interface for the class SemanticIndexEngine that build the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {



	public int getIndex(Description d);
	public List<Interval> getIntervals(Description d);
	public Map<Description, Integer> getIndexes();
	public Map<Description, SemanticIndexRange> getIntervals();
}
