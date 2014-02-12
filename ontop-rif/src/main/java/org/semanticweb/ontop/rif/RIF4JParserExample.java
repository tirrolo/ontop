package org.semanticweb.ontop.rif;

import java.net.URI;

import at.sti2.rif4j.condition.Formula;
import at.sti2.rif4j.manager.DocumentLoadingException;
import at.sti2.rif4j.manager.DocumentManager;
import at.sti2.rif4j.rule.Document;

public class RIF4JParserExample {

	public static void main(String[] args) throws DocumentLoadingException {
		URI premiseUri = URI
                .create("http://www.w3.org/2005/rules/test/repository/"
                        + "tc/Class_Membership/Class_Membership-premise.rif");

        // The URI of the conclusion formula.
        URI conclusionUri = URI
                .create("http://www.w3.org/2005/rules/test/repository/"
                        + "tc/Class_Membership/Class_Membership-conclusion.rif");

        // Use the DocumentManager to load the premise and the conclusion.
        DocumentManager manager = new DocumentManager();
        Document premise = manager.loadDocument(premiseUri);
        Formula conclusion = manager.loadFormula(conclusionUri);
        
        System.out.println("premise:");

        System.out.println(premise);

        System.out.println();

        System.out.println("conclusion:");

        System.out.println(conclusion);
	}
}
