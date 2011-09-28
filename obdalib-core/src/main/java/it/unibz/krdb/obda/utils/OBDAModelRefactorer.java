package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

import org.semanticweb.owl.model.OWLOntology;

public class OBDAModelRefactorer {
	
	private OBDAModel obdaModel;
	private TargetQueryValidator validator;
	
	// TODO We should reduce the dependency to OWL-API to define the ontology.
	public OBDAModelRefactorer(OBDAModel obdaModel, OWLOntology ontology) {
		this.obdaModel = obdaModel;
		validator = new TargetQueryValidator(ontology);
	}
	
	public void run() throws Exception {
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappingTable = obdaModel.getMappings();
		for (URI datasourceUri : mappingTable.keySet()) {
			for (OBDAMappingAxiom mapping : mappingTable.get(datasourceUri)) {
				CQIE tq = (CQIE)mapping.getTargetQuery();
				boolean bSuccess = validator.validate(tq);
				if (!bSuccess) {
					throw new Exception("Found an invalid target query: " + tq.toString());
				}
			}
		}
	}
}
