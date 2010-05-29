package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

public class MappingHeadVariableTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {
	//Accepts any mapping containing the variable in any of its head atoms
	private String srtHeadVariableFilter;

	public MappingHeadVariableTreeModelFilter(String srtHeadVariableFilter) {
		this.srtHeadVariableFilter = srtHeadVariableFilter;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		// TODO Auto-generated method stub
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();
		ArrayList<QueryAtom> atoms = headquery.getAtoms();
		int atomscount = atoms.size();
		for (int i = 0; i < atomscount; i++) {
			QueryAtom atom = atoms.get(i);
			ArrayList<QueryTerm> queryTerms = atom.getTerms();
			int termscount = queryTerms.size();
			for (int j = 0; j < termscount; j++) {
				QueryTerm term = queryTerms.get(j);
				if (term instanceof VariableTerm) {
					if (term.toString().indexOf(srtHeadVariableFilter) != -1)
						filterValue = true;
				}
			}
		}
		return filterValue;
	}

}
