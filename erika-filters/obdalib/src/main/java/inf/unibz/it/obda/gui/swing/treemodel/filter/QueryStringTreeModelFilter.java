package inf.unibz.it.obda.gui.swing.treemodel.filter;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

public class QueryStringTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String srtQueryTreeFilter;

	public QueryStringTreeModelFilter(String srtQueryTreeFilter) {
		this.srtQueryTreeFilter = srtQueryTreeFilter;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		// TODO Auto-generated method stub
		boolean filterValue = false;
		try {
			OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
			RDBMSSQLQuery q = new RDBMSSQLQuery();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filterValue;
	}
}
