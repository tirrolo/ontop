package sesameWrapper;

import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import org.openrdf.query.Dataset;

public class SesameClassicInMemoryRepo extends SesameClassicRepo {
	
	private static QuestPreferences p = new QuestPreferences();

	public SesameClassicInMemoryRepo(String name, String tboxFile)
			throws Exception {
		
		super();
		
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC); 
		p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
		
		createStore(name, tboxFile, p); 
		
	}
	
	public SesameClassicInMemoryRepo(String name, Dataset data)
			throws Exception {
		
		super();
		
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC); 
		p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
		
		createStore(name, data, p); 
		
	}



}
