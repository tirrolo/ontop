package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;

public class MappingPredicateTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String srtPredicateFilter;

	public MappingPredicateTreeModelFilter(String srtPredicateFilter) {
		this.srtPredicateFilter = srtPredicateFilter;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();
		ArrayList<QueryAtom> atoms = headquery.getAtoms();
		int atomscount = atoms.size();
		for (int i = 0; i < atomscount; i++) {
			QueryAtom atom = atoms.get(i);
			
			if (atom.getName().indexOf(srtPredicateFilter) != -1)
				filterValue = true;

		}
		return filterValue;
	}

}
