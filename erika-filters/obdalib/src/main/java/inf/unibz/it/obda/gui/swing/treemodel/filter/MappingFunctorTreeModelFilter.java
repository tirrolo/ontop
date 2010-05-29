package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

public class MappingFunctorTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String strMappingFunctor = "";

	public MappingFunctorTreeModelFilter(String strMappingFunctor) {
		this.strMappingFunctor = strMappingFunctor;
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

			ArrayList<QueryTerm> queryTerms = atom.getTerms();
			int termscount = queryTerms.size();
			for (int j = 0; j < termscount; j++) {
				QueryTerm term = queryTerms.get(j);
				if (term instanceof VariableTerm) {
					if (term.toString().indexOf(strMappingFunctor) != -1)
						filterValue = true;
				}
				else if (term instanceof FunctionTerm) {
					FunctionTerm functor = (FunctionTerm) term;
					ArrayList<QueryTerm> functorTerms = functor.getParameters();
					for (QueryTerm funcTerm : functorTerms) {
						if (funcTerm instanceof VariableTerm) {
							if ((funcTerm.toString().indexOf(strMappingFunctor) != -1)) {
								filterValue = true;
							}
						}
					}
				}

			}
		}

		return filterValue;
	}

}
