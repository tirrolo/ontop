package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi3.bootstrapping.DirectMappingBootstrapper;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.event.EventType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapAction extends ProtegeAction {

	private OWLEditorKit editorKit = null;
	private OWLWorkspace workspace;	
	private OWLModelManager owlManager;
	private OBDAModelManager modelManager;
	private DirectMappingBootstrapper dm = null;
	
	private Logger log = LoggerFactory.getLogger(BootstrapAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();	
		workspace = editorKit.getWorkspace();	
		owlManager = editorKit.getOWLModelManager();
		modelManager = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName()));
	}

	@Override
	public void dispose() throws Exception {
		//NOP
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		OWLOntology currentOnto = owlManager.getActiveOntology();
		OBDAModel currentModel = modelManager.getActiveOBDAModel();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Choose a datasource to bootstrap: "), BorderLayout.NORTH);
		List<String> options = new ArrayList<String>();
		for (OBDADataSource source : currentModel.getSources())
			options.add(source.getSourceID().toString());
		JComboBox combo = new JComboBox(options.toArray());
		int index = combo.getSelectedIndex();

		OBDADataSource currentSource = currentModel.getSources().get(index);
		if (currentSource != null) {
			try {
				dm = new DirectMappingBootstrapper(currentOnto, currentModel,
						currentSource);
				currentModel = dm.getModel();
				currentOnto = dm.getOntology();
				currentModel.fireSourceParametersUpdated();
				
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				JOptionPane.showMessageDialog(null,	"ERROR occured during bootstrapping. ");
			}
		}
	}

	
}
