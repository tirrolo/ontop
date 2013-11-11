/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.testsuite;

import org.semanticweb.ontop.quest.scenarios.Db2VirtualScenarioTest;
import org.semanticweb.ontop.quest.scenarios.MssqlVirtualScenarioTest;
import org.semanticweb.ontop.quest.scenarios.MysqlVirtualScenarioTest;
import org.semanticweb.ontop.quest.scenarios.OracleVirtualScenarioTest;
import org.semanticweb.ontop.quest.scenarios.PgsqlVirtualScenarioTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestVirtualScenarioSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Scenario Tests for Quest in Virtual mode");
		suite.addTest(MysqlVirtualScenarioTest.suite());
		suite.addTest(PgsqlVirtualScenarioTest.suite());
		suite.addTest(MssqlVirtualScenarioTest.suite());
		suite.addTest(OracleVirtualScenarioTest.suite());
		suite.addTest(Db2VirtualScenarioTest.suite());
		return suite;
	}
}
