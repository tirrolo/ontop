package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore;

import java.io.File;
import java.net.URI;

import org.openrdf.repository.RepositoryException;

public class SesameVirtualRepo extends SesameAbstractRepo {

	private QuestDBVirtualStore virtualStore;
	private QuestDBConnection questDBConn;

	public SesameVirtualRepo(String name, String tboxFile, String obdaFile, boolean existential, String rewriting)
			throws Exception {
		super();

		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		if (rewriting.equals("TreeWitness"))
			pref.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TW);
		else if (rewriting.equals("Default"))
			pref.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.UCQBASED);
		
		
		URI obdaURI = (new File(obdaFile)).toURI();
		this.virtualStore = new QuestDBVirtualStore(name,
				(new File(tboxFile)).toURI(), obdaURI, pref);
		questDBConn = virtualStore.getConnection();
	}

	
	@Override
	public QuestDBConnection getQuestConnection() throws OBDAException {
		return questDBConn;
	}

	@Override
	public boolean isWritable() throws RepositoryException {
		// Checks whether this repository is writable, i.e.
		// if the data contained in this repository can be changed.
		// The writability of the repository is determined by the writability
		// of the Sail that this repository operates on.
		return false;
	}
	
	@Override
	public void shutDown() throws RepositoryException {
		super.shutDown();
		try {
			questDBConn.close();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}

	public String getType() {
		return QuestConstants.VIRTUAL;
	}

}
