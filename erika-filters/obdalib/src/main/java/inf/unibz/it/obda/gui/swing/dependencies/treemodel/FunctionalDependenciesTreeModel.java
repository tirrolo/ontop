package inf.unibz.it.obda.gui.swing.dependencies.treemodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;
import inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter;

import javax.swing.tree.MutableTreeNode;

/**
 * A modified tree model, adapted to the needs for handling functional
 * dependencies
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public class FunctionalDependenciesTreeModel extends
AssertionControllerTreeModel<RDBMSFunctionalDependency> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1681721482145020281L;
	
	public FunctionalDependenciesTreeModel(MutableTreeNode root,
			AssertionController<RDBMSFunctionalDependency> conroller,
			AssertionTreeNodeFactory<RDBMSFunctionalDependency> factory) {
		super(root, conroller, factory);
//		List<RDBMSFunctionalDependency> list = funcController.getDependenciesForCurrentDataSource();
//		if(list != null){
//			Iterator<RDBMSFunctionalDependency> it = list.iterator();
//			while(it.hasNext()){
//				AssertionTreeNode<RDBMSFunctionalDependency> node = factory.render(it.next());
//				insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
//				nodeStructureChanged(root);
//			}
//		}
	}
	
	public void synchronize() {
		Collection<RDBMSFunctionalDependency> assertions = ((RDBMSFunctionalDependencyController)controller).getDependenciesForCurrentDataSource();
		for (RDBMSFunctionalDependency assertion : assertions) {
			AssertionTreeNode<RDBMSFunctionalDependency> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
	
	public void addAssertions(HashSet<RDBMSFunctionalDependency> assertinos){
		Iterator<RDBMSFunctionalDependency> it = assertinos.iterator();
		while(it.hasNext()){
			AssertionTreeNode<RDBMSFunctionalDependency> node = renderer.render(it.next());
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
