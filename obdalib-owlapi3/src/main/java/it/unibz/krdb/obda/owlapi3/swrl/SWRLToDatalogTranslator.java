package it.unibz.krdb.obda.owlapi3.swrl;

import java.util.Collection;
import java.util.List;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLRule;

/**
 * Class that allows to create Datalog program from SWRL rules in an ontology
 * 
 */
public class SWRLToDatalogTranslator {

	Collection<CQIE> rules;

	public SWRLToDatalogTranslator(OWLOntology ontology) {

		SWRLVisitor visitor = new SWRLVisitor();

		// get the axioms from the ontology and consider only the rules
		rules = visitor.createDatalog(ontology);
	}

	public SWRLToDatalogTranslator(SWRLRule rule) {

		SWRLVisitor visitor = new SWRLVisitor();
		// transform the rule
		rules = visitor.createDatalog(rule);

	}

	public Collection<CQIE> getRules() {
		return rules;
	}

	public static void addSWRLRulesToProgram(DatalogProgram p, OWLOntology ontology) {
		Collection<CQIE> rules = new SWRLToDatalogTranslator(ontology).getRules();
		p.appendRule(rules);

	}
}
