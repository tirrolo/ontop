package inf.unibz.it.obda.codec.xml;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;
import inf.unibz.it.utils.codec.ObjectXMLCodec;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DatasourceXMLCodec extends ObjectXMLCodec<DataSource> {

	private static final String	TAG	= "dataSource";

	@Override
	public DataSource decode(Element input) {

 		DataSource new_src = null;

		String id = input.getAttribute("URI");
		if(id.equals("")){
			id = input.getAttribute("name"); //old version
		}
		String uristring = input.getAttribute("ontouri");
		String driver = input.getAttribute("databaseDriver");
		String dbname = input.getAttribute("databaseName");
		String pwd = input.getAttribute("databasePassword");
		String dburl = input.getAttribute("databaseURL");
		String username = input.getAttribute("databaseUsername");
		URI uri = URI.create(id);
		new_src = new DataSource(uri);
		new_src.setParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER, driver);
		new_src.setParameter(RDBMSsourceParameterConstants.DATABASE_NAME, dbname);
		new_src.setParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD, pwd);
		new_src.setParameter(RDBMSsourceParameterConstants.DATABASE_URL, dburl);
		new_src.setParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME, username);
		new_src.setParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI, uristring);

		/***
		 * This if is only done because before, URI and name were used
		 * interchangable. Since now URI stands for the ontology URI we check if
		 * they are the same, if the are, it means its an old file and the URI
		 * is set to the current ontlogy's URI
		 */
//		if (!name.equals(uri)) {
//			new_src.setUri(uri);
//		} else {
//			throw new IllegalArgumentException("WARNING: A data source read form the .obda file has name=ontologyURI, this .obda file might be a deprecated file. Replace ontologyURI with the real URI of the ontology.");
////			APIController controller = APIController.getController();
////			URI currentOntologyURI = controller.getCurrentOntologyURI();
////			new_src.setUri(currentOntologyURI.toString());
//
//		}
		return new_src;

	}

	@Override
	public Element encode(DataSource input) {
		Element element = createElement(TAG);
		String id = input.getSourceID().toString();
		String driver = input.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER);
		String  uristring= input.getParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI);
		String dbname = input.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
		String pwd = input.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD);
		String dburl = input.getParameter(RDBMSsourceParameterConstants.DATABASE_URL);
		String username = input.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME);
		
		element.setAttribute("URI", id);
		element.setAttribute("ontouri",uristring);
		element.setAttribute("databaseDriver",driver);
		element.setAttribute("databaseName", dbname);
		element.setAttribute("databasePassword", pwd);
		element.setAttribute("databaseURL", dburl);
		element.setAttribute("databaseUsername", username);
		return element;
	}

	public Collection<String> getAttributes() {
		ArrayList<String> fixedAttributes = new ArrayList<String>();
		fixedAttributes.add("name");
		fixedAttributes.add("URI");
		return fixedAttributes;
	}

	public String getElementTag() {
		return TAG;
	}

}
