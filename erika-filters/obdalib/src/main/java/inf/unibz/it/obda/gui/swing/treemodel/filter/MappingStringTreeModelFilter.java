package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.net.URI;
import java.util.ArrayList;

import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

//Receives a mapping
//Check string in head or body
public class MappingStringTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {
	private String srtModelFilter;

	public MappingStringTreeModelFilter(String strModeFilter) {
		this.srtModelFilter = strModeFilter;
	}

	
	@Override
	public boolean match(OBDAMappingAxiom object) {
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();
		RDBMSSQLQuery bodyquery = (RDBMSSQLQuery) mapping.getSourceQuery();
		
		ArrayList<QueryAtom> atoms = headquery.getAtoms();
		int atomscount = atoms.size();
		for (int i = 0; i < atomscount; i++) {
			QueryAtom atom = atoms.get(i);
			ArrayList<QueryTerm> queryTerms = atom.getTerms();
			int termscount = queryTerms.size();
			for (int j = 0; j < termscount; j++) {
				QueryTerm term = queryTerms.get(j);
				if (term.toString().indexOf(srtModelFilter) != -1)
					filterValue = true;

			}
		}
		if (bodyquery.getInputQuString().indexOf(srtModelFilter) != -1)
			filterValue = true;

		return filterValue;
	}

}
