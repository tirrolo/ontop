package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.S_QuestConnection;

import org.semanticweb.owlapi.model.OWLException;

public class S_QuestOWLConnection implements OWLConnection {

	private final S_QuestConnection conn;

	public S_QuestOWLConnection(S_QuestConnection conn) {
		this.conn = conn;
	}

	@Override
	public void close() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public OWLStatement createStatement() throws OWLException {
		try {
			return new S_QuestOWLStatement(conn.createStatement(), this);
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public void commit() throws OWLException {
		try {
			conn.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public void setAutoCommit(boolean autocommit) throws OWLException {
		try {
			conn.setAutoCommit(autocommit);
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public boolean getAutoCommit() throws OWLException {
		try {
			return conn.getAutoCommit();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public boolean isClosed() throws OWLException {
		try {
			return conn.isClosed();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public boolean isReadOnly() throws OWLException {
		try {
			return conn.isReadOnly();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public void rollBack() throws OWLException {
		try {
			conn.rollBack();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

}
