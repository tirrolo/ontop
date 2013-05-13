package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange.Interval;

import java.util.List;
import java.util.Map;

/** An interface for the class SemanticIndexEngine that build the indexes for the DAG
 * 
 */

public interface SemanticIndexEngine {


	//given the description returns the index
	public int getIndex(Description d);
	
	//obtain the list of intervals (with indexes of the descendants) for the given description
	public List<Interval> getIntervals(Description d);
	
	//obtain the map with all the indexes
	public Map<Description, Integer> getIndexes();
	
	//obtain the map wiht all the intervals
	public Map<Description, SemanticIndexRange> getIntervals();
}
