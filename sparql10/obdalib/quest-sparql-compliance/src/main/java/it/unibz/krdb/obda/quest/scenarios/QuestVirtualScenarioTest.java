package it.unibz.krdb.obda.quest.scenarios;

import junit.framework.Test;

import org.openrdf.repository.Repository;

import sesameWrapper.SesameVirtualRepo;


public class QuestVirtualScenarioTest extends QuestScenarioTest {

	public QuestVirtualScenarioTest(String testURI, String name,
			String queryFileURL, String resultFileURL, String owlFileURL,
			String obdaFileURL, String parameterFileURL) {
		super(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, parameterFileURL);
	}

	public static Test suite() throws Exception {
		return ScenarioManifestTest.suite(new Factory() {
			@Override
			public QuestVirtualScenarioTest createQuestScenarioTest(String testURI, String name, String queryFileURL, 
					String resultFileURL, String owlFileURL, String obdaFileURL) {
				return createQuestScenarioTest(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, "");
			}
			@Override
			public QuestVirtualScenarioTest createQuestScenarioTest(String testURI, String name, String queryFileURL, 
					String resultFileURL, String owlFileURL, String obdaFileURL, String parameterFileURL) {
				return new QuestVirtualScenarioTest(testURI, name, queryFileURL, resultFileURL, owlFileURL, 
						obdaFileURL, parameterFileURL);
			}
			@Override
			public String getMainManifestFile() {
				return "/testcases-scenarios/virtual-mode/manifest-scenario.ttl";
			}
		});
	}
	
	@Override
	protected Repository createRepository() throws Exception {
		try {
			SesameVirtualRepo repo = new SesameVirtualRepo(getName(), owlFileURL, obdaFileURL, parameterFileURL);
			repo.initialize();
			return repo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}