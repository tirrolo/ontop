/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */

package inf.unibz.it.obda.gui.swing.dataquery.panel;

import inf.unibz.it.obda.gui.swing.action.OBDADataQueryAction;
import inf.unibz.it.obda.gui.swing.datasource.panels.IncrementalResultSetTableModel;
import inf.unibz.it.utils.swing.DialogUtils;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.TableModel;

//import edu.stanford.smi.protegex.owl.ui.ProtegeUI;

/**
 * 
 * @author mariano
 */
public class ResultViewTablePanel extends javax.swing.JPanel {

	private OBDADataQueryAction			countAllTuplesAction	= null;
	private OBDADataQueryAction			countAllTuplesActionEQL	= null;
	private QueryInterfacePanel 		querypanel = null;
	
	/** Creates new form ResultViewTablePanel */
	public ResultViewTablePanel(QueryInterfacePanel panel) {
		querypanel = panel;
		initComponents();
		addPopUpMenu();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panellViewSourceSquemaButtons = new javax.swing.JPanel();
        buttonImport = new javax.swing.JButton();
        buttonSaveResults = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();

        setMinimumSize(new java.awt.Dimension(400, 480));
        setLayout(new java.awt.BorderLayout());

        panellViewSourceSquemaButtons.setMinimumSize(new java.awt.Dimension(100, 480));
        panellViewSourceSquemaButtons.setPreferredSize(new java.awt.Dimension(100, 45));
        panellViewSourceSquemaButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonImport.setText("Import as ABox assertions");
        buttonImport.setEnabled(false);
        buttonImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonImportActionPerformed(evt);
            }
        });
        panellViewSourceSquemaButtons.add(buttonImport);

        buttonSaveResults.setText("Save results");
        buttonSaveResults.setEnabled(false);
        buttonSaveResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveResultsActionPerformed(evt);
            }
        });
        panellViewSourceSquemaButtons.add(buttonSaveResults);

        add(panellViewSourceSquemaButtons, java.awt.BorderLayout.SOUTH);

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Results"
            }
        ));
        jScrollPane1.setViewportView(resultsTable);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
        
        this.resultsTable.setFont(new Font("Arial", Font.PLAIN, 18));
        this.resultsTable.setRowHeight(21);
    }// </editor-fold>//GEN-END:initComponents

	private void resultsTableMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_resultsTableMouseClicked
	// TODO add your handling code here:
	}// GEN-LAST:event_resultsTableMouseClicked

	private void buttonImportActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_buttonImportActionPerformed
//		protege_main_window = (JFrame) getParent().getParent().getParent().getParent().getParent().getParent().getParent();
//		String message = "<html>This will incorporate all knowledge presented in the results <br> as assertions about individuals in the current project. <br> <br> <bold>Are you sure you want to proceed?</bold></html>";
//		JOptionPane.showConfirmDialog(protege_main_window, message);

	}// GEN-LAST:event_buttonImportActionPerformed


	private void buttonSaveResultsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_buttonSaveResultsActionPerformed
		//Component tlcontainer = ProtegeUI.getTopLevelContainer(OBDAPluginController.getCurrentInstance().getCurrentProject());
		//JFrame protege_main_window = (JFrame) getParent().getParent().getParent().getParent().getParent().getParent().getParent();
		JDialog saveDialog = new JDialog();
		SaveAsDialogPanel savePanel = new SaveAsDialogPanel(saveDialog);
		saveDialog.getContentPane().add(savePanel, java.awt.BorderLayout.CENTER);
		saveDialog.pack();
		DialogUtils.centerDialogWRTParent(this, saveDialog);
		saveDialog.setVisible(true);
	}// GEN-LAST:event_buttonSaveResultsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonImport;
    private javax.swing.JButton buttonSaveResults;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panellViewSourceSquemaButtons;
    private javax.swing.JTable resultsTable;
    // End of variables declaration//GEN-END:variables
	
	public void setTableModel(final TableModel newmodel) {
		Runnable updateModel = new Runnable() {
			public void run() {
				
				resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
				ToolTipManager.sharedInstance().unregisterComponent(resultsTable);
				ToolTipManager.sharedInstance().unregisterComponent(resultsTable.getTableHeader());

				TableModel oldmodel = resultsTable.getModel();
				if (oldmodel != null) {
					oldmodel.removeTableModelListener(resultsTable);
					if (oldmodel instanceof IncrementalResultSetTableModel) {
						IncrementalResultSetTableModel incm = (IncrementalResultSetTableModel) oldmodel;
						incm.close();
					}
				}
				//QueryResultTableModel newmodel = (QueryResultTableModel) runner.getResult();
				resultsTable.setModel(newmodel);
//				newmodel.addTableModelListener(resultsTable);
				// newmodel.fireTableStructureChanged();

				
				//				newmodel.fireTableDataChanged();

				// resultsTable.setPreferredScrollableViewportSize(r);
//				Dimension d = new Dimension(150 * newmodel.getColumnCount(), resultsTable.getRowHeight()
//						* newmodel.getRowCount());
//				resultsTable.setPreferredSize(d);
//				resultsTable.setMinimumSize(d);
//				resultsTable.setMaximumSize(d);
//				resultsTable.setPreferredScrollableViewportSize(d);
//				resultsTable.setR
				addNotify();
				
				//resultsTable.setPreferredSize(d);
				// resultsTable.getR
				// JTable t = new JTable(newmodel);
				// Container parent = resultsTable.getParent();
				// JScrollPane pane = null;
				// pane.set
				// parent.invalidate();
				// parent.repaint();

				resultsTable.invalidate();
				resultsTable.repaint();
			}
		};
		SwingUtilities.invokeLater(updateModel);
	}
	
	private void addPopUpMenu(){
		JPopupMenu menu = new JPopupMenu();
		JMenuItem countAll = new JMenuItem(); 
		countAll.setText("count all tuples");
		countAll.addActionListener(new ActionListener(){

//			@Override
			public void actionPerformed(ActionEvent e) {
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						int tab =querypanel.getSelecetTab();
						if(tab == 0){
							String query = querypanel.getSPARQLQuery();
							getCountAllTuplesActionForUCQ().run(query, querypanel);
						}else{
							String query = querypanel.getSPARQLQuery();
							getCountAllTuplesActionForEQL().run(query, querypanel);
						}
					}
				});
			}
			
		});
		menu.add(countAll);
		resultsTable.setComponentPopupMenu(menu);
	}

	public OBDADataQueryAction getCountAllTuplesActionForUCQ() {
		return countAllTuplesAction;
	}

	public void setCountAllTuplesActionForUCQ(OBDADataQueryAction countAllTuples) {
		this.countAllTuplesAction = countAllTuples;
	}
	
	public OBDADataQueryAction getCountAllTuplesActionForEQL() {
		return countAllTuplesActionEQL;
	}

	public void setCountAllTuplesActionForEQL(OBDADataQueryAction countAllTuples) {
		this.countAllTuplesActionEQL = countAllTuples;
	}
	
	

}
