package inf.unibz.it.obda.gui.swing.constraints.treemodel;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.controller.RDBMSPrimaryKeyConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;
import inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.MutableTreeNode;

public class PrimaryKeyConstraintTreeModel extends
AssertionControllerTreeModel<RDBMSPrimaryKeyConstraint>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -232684124546544031L;

	public PrimaryKeyConstraintTreeModel(MutableTreeNode root,
			AssertionController<RDBMSPrimaryKeyConstraint> conroller,
			AssertionTreeNodeFactory<RDBMSPrimaryKeyConstraint> factory) {
		super(root, conroller, factory);
		// TODO Auto-generated constructor stub
	}

	public void synchronize() {
		Collection<RDBMSPrimaryKeyConstraint> assertions = ((RDBMSPrimaryKeyConstraintController)controller).getDependenciesForCurrentDataSource();
		for (RDBMSPrimaryKeyConstraint assertion : assertions) {
			AssertionTreeNode<RDBMSPrimaryKeyConstraint> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
	
	public void addAssertions(HashSet<RDBMSPrimaryKeyConstraint> assertinos){
		Iterator<RDBMSPrimaryKeyConstraint> it = assertinos.iterator();
		while(it.hasNext()){
			AssertionTreeNode<RDBMSPrimaryKeyConstraint> node = renderer.render(it.next());
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
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
