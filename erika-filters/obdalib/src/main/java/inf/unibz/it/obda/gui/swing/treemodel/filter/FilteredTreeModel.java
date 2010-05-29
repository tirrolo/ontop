package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.*;

/**
 * 
 *
 */
public interface FilteredTreeModel {

	public void addFilter(TreeModelFilter T);

	public void addFilters(List<TreeModelFilter> T);

	public void removeFilter(TreeModelFilter T);

	public void removeFilter(List<TreeModelFilter> T);

	public void removeAllFilters();

}
