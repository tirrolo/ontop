package inf.unibz.it.obda.gui.swing.treemodel.filter;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

public class MappingSQLStringTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String srtSQLStringTreeModelFilter;

	public MappingSQLStringTreeModelFilter(String srtSQLStringTreeModelFilter) {
		this.srtSQLStringTreeModelFilter=srtSQLStringTreeModelFilter;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean match(OBDAMappingAxiom object) {
		// TODO Auto-generated method stub
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		RDBMSSQLQuery bodyquery = (RDBMSSQLQuery) mapping.getSourceQuery();
		if (bodyquery.getInputQuString().indexOf(srtSQLStringTreeModelFilter) != -1)
			filterValue = true;
		return filterValue;
	}
}
