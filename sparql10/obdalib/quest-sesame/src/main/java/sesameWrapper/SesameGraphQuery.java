package sesameWrapper;

import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class SesameGraphQuery implements GraphQuery {

	private static final long serialVersionUID = 1L;

	private String queryString, baseURI;
	private QuestDBStatement stm;
	private ValueFactory fact = new ValueFactoryImpl();

	public SesameGraphQuery(String queryString, String baseURI,
			QuestDBStatement statement) throws MalformedQueryException {
		if (queryString.toLowerCase().contains("construct") || queryString.toLowerCase().contains("describe")) {
			this.queryString = queryString;
			this.baseURI = baseURI;
			this.stm = statement;
		} else
			throw new MalformedQueryException("Graph query expected!");
	}

	public void setMaxQueryTime(int maxQueryTime) {
		try {
			stm.setQueryTimeout(maxQueryTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getMaxQueryTime() {
		try {
			return stm.getQueryTimeout();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setBinding(String name, Value value) {
		// TODO Auto-generated method stub

	}

	public void removeBinding(String name) {
		// TODO Auto-generated method stub

	}

	public void clearBindings() {
		// TODO Auto-generated method stub

	}

	public BindingSet getBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	public Dataset getDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setIncludeInferred(boolean includeInferred) {
		// always true

	}

	public boolean getIncludeInferred() {
		return true;
	}

	private Statement createStatement(Assertion assertion) {

		return new SesameStatement(assertion);
	}

	public GraphQueryResult evaluate() throws QueryEvaluationException {

		try {
			// execute query and return new type of result
			GraphResultSet res = stm.executeConstruct(queryString);
			Map<String, String> namespaces = new HashMap<String, String>();

			List<Statement> results = new LinkedList<Statement>();
			while (res.hasNext()) {
				List<Assertion> chunk = res.next();
				for (Assertion as : chunk) {
					Statement st = createStatement(as);
					results.add(st);
				}
			}

			return new GraphQueryResultImpl(namespaces, results.iterator());

		} catch (OBDAException e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e.getMessage());
		}
	}

	public void evaluate(RDFHandler handler) throws QueryEvaluationException,
			RDFHandlerException {
		GraphQueryResult result =  evaluate();
		handler.startRDF();
		while (result.hasNext())
			handler.handleStatement(result.next());
		handler.endRDF();

	}

}
