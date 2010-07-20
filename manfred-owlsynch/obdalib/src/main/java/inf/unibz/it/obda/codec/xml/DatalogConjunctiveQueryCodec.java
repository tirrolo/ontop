package inf.unibz.it.obda.codec.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Element;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;
import inf.unibz.it.utils.codec.ObjectXMLCodec;
import inf.unibz.it.utils.codec.TargetQeryToTextCodec;

public class DatalogConjunctiveQueryCodec extends ObjectXMLCodec<ConjunctiveQuery> {

	private static final String	TAG	= "CQ";
	APIController apic = null;
	
	public DatalogConjunctiveQueryCodec(APIController apic){
		this.apic = apic;
	}
	
	@Override
	public ConjunctiveQuery decode(Element input) {
		
		String CQstring = input.getAttribute("string");
		ConjunctiveQuery cq=null;
		try {
			cq = new ConjunctiveQuery(CQstring, apic);
		} catch (QueryParseException e1) {
//			throw e1;
			return null;
		}
		return cq;
	}

	@Override
	public Element encode(ConjunctiveQuery hq) {
		
		Element mappingheadelement = createElement(TAG);
		TargetQeryToTextCodec codec = new TargetQeryToTextCodec(apic);
		mappingheadelement.setAttribute("string", codec.encode(hq));
		return mappingheadelement;
	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> fixedAttributes = new ArrayList<String>();
		fixedAttributes.add("string");
		return fixedAttributes;
	}

	@Override
	public String getElementTag() {
		// TODO Auto-generated method stub
		return TAG;
	}
	
	public ConjunctiveQuery decode(String input) {
		
		ConjunctiveQuery cq=null;
		try {
			cq = new ConjunctiveQuery(input, apic);
		} catch (QueryParseException e1) {
//			throw e1;
			return null;
		}
		return cq;
	}

}
