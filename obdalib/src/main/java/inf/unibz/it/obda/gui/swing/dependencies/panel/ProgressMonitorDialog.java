package inf.unibz.it.obda.gui.swing.dependencies.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewMonotorDialog.java
 *
 * Created on Aug 24, 2009, 11:41:46 AM
 */

/**
 *A simple dialog, which show during the dependency mining process
 *
  * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 */
public class ProgressMonitorDialog {

	/**
	 * the dialog
	 */
	JDialog window;
	/**
	 * the parent of the dialog
	 */
	JPanel parent;
	/**
	 * the content of the Label 
	 */
	String labelcontent = null;
	/**
	 * boolean field indicating whether the mining process was interrupted or not
	 */
	boolean stopped = false;
	
	
    /** Creates new form NewMonotorDialog */
    public ProgressMonitorDialog(Dependency_SelectMappingPane panel) {
    	parent = panel;
        initComponents();
        window.setLocation(parent.getLocationOnScreen());
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
               ((Dependency_SelectMappingPane)parent).cancelMining();
            }
        	
        });
        jButton1.setText("Cancel");
        jButton1.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(!stopped){
					 ((Dependency_SelectMappingPane)parent).cancelMining();
				}
			}
        	
        });
    }
    
    public ProgressMonitorDialog(JPanel panel, String s) {
    	
    	parent = panel;
        initComponents();
        window.setLocation(parent.getLocationOnScreen().x+50,parent.getLocationOnScreen().y+50);
        jButton1.setVisible(false);
        jLabel1.setText(s);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        
        window = new JDialog(new JFrame(), true);
        window.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        window.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Mining Inclusion Dependencies...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 7, 6);
        window.getContentPane().add(jLabel1, gridBagConstraints);

        jProgressBar1.setIndeterminate(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 7, 6);
        window.getContentPane().add(jProgressBar1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
        window.getContentPane().add(jLabel2, gridBagConstraints);

        jButton1.setText("jButton1");
        jButton1.setMaximumSize(new java.awt.Dimension(75, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(75, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(75, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        window.getContentPane().add(jButton1, gridBagConstraints);

        window. pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    
    /**
     * Starts the JDialog in an own thread
     */
    public void show() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	window.setVisible(true);
            }
        });
    }

    /**
     * Stops the dialog and sets it to not visible
     */
    public void stop() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	stopped = true;
            	window.setVisible(false);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JProgressBar jProgressBar1;
    private JButton jButton1;
    // End of variables declaration//GEN-END:variables

}
