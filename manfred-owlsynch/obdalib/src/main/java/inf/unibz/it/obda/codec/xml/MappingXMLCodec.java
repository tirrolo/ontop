package inf.unibz.it.obda.codec.xml;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;
import inf.unibz.it.utils.codec.ObjectXMLCodec;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MappingXMLCodec extends ObjectXMLCodec<OBDAMappingAxiom>{

	private static final String	TAG	= "mapping";
	APIController apic = null;
	DatalogConjunctiveQueryCodec cqcodec = null;
	
	public MappingXMLCodec(APIController apic){
		this.apic = apic;
		cqcodec = new DatalogConjunctiveQueryCodec(apic);
	}
	
	@Override
	public OBDAMappingAxiom decode(Element mapping) {
		
		String id = mapping.getAttribute("id");
		Element head = null;
		Element body = null;
		NodeList mappingchilds = mapping.getChildNodes();
		// Retrieving the child nodes avoiding empty nodes
		for (int j = 0; j < mappingchilds.getLength(); j++) {
			Node mappingchild = mappingchilds.item(j);
			if (!(mappingchild instanceof Element)) {
				continue;
			}
			if (head == null) {
				head = (Element) mappingchild;
				continue;
			}

			if (body == null) {
				body = (Element) mappingchild;
				continue;
			}
		}
		String SQLstring = body.getAttribute("string");
		
		ConjunctiveQuery headquery = cqcodec.decode(head);
		if(headquery == null){
			return null;
		}
		RDBMSSQLQuery bodyquery=null;
		RDBMSOBDAMappingAxiom newmapping=null;
		try {
			bodyquery = new RDBMSSQLQuery(SQLstring, apic);
			newmapping = new RDBMSOBDAMappingAxiom(id);
		} catch (QueryParseException e) {
			e.printStackTrace();
		}
		newmapping.setSourceQuery(bodyquery);
		newmapping.setTargetQuery(headquery);
		return newmapping;
	}

	@Override
	public Element encode(OBDAMappingAxiom input) {
		
		Element mappingelement = createElement(TAG);
		// the new XML mapping
		mappingelement.setAttribute("id", input.getId());
		// the head XML child
		ConjunctiveQuery hq = (ConjunctiveQuery) input.getTargetQuery();
		Element mappingheadelement = cqcodec.encode(hq);
		// the body XML child
		Element mappingbodyelement = createElement("SQLQuery");
		RDBMSSQLQuery bq = (RDBMSSQLQuery) input.getSourceQuery();
		if(bq != null){
			mappingbodyelement.setAttribute("string", bq.toString());
		}else{
			mappingbodyelement.setAttribute("string", "");
		}
		mappingelement.getOwnerDocument().adoptNode(mappingheadelement);
		mappingelement.appendChild(mappingheadelement);
		mappingelement.appendChild(mappingbodyelement);
		
		return mappingelement;
	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> fixedAttributes = new ArrayList<String>();
		fixedAttributes.add("id");
		return fixedAttributes;
	}

	@Override
	public String getElementTag() {
		return TAG;
	}

}
