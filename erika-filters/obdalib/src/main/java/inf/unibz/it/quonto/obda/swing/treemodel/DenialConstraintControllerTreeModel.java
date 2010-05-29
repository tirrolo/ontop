package inf.unibz.it.quonto.obda.swing.treemodel;

import java.util.List;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;
import inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter;
import inf.unibz.it.quonto.dl.assertion.DenialConstraint;

import javax.swing.tree.MutableTreeNode;

public class DenialConstraintControllerTreeModel extends AssertionControllerTreeModel<DenialConstraint> {

	private static final long	serialVersionUID	= 7789001092385914107L;

	public DenialConstraintControllerTreeModel(MutableTreeNode root, AssertionController<DenialConstraint> conroller, AssertionTreeNodeFactory<DenialConstraint> renderer) {
		super(root, conroller, renderer);
	}

	@Override
	public void addFilter(TreeModelFilter T) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFilters(List<TreeModelFilter> T) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllFilters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFilter(TreeModelFilter T) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFilter(List<TreeModelFilter> T) {
		// TODO Auto-generated method stub
		
	}

}
