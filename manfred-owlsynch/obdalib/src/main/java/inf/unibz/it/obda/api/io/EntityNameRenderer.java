package inf.unibz.it.obda.api.io;

import java.net.URI;

import inf.unibz.it.dl.domain.NamedPredicate;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.TypedConstantTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

public class EntityNameRenderer {

	protected APICoupler coupler = null;

	public String getPredicateName(BinaryQueryAtom bqa){
		NamedPredicate np = bqa.getNamedPredicate();
		URI uri = np.getUri();
		String prefix = coupler.getPrefixForUri(uri);
		return prefix+":"+uri.getFragment();
	}
	
	public String getPredicateName(QueryAtom qa){
		if(qa instanceof BinaryQueryAtom){
			return getPredicateName((BinaryQueryAtom)qa);
		}else{
			return getPredicateName((ConceptQueryAtom)qa);
		}
	}
	
	public String getPredicateName(ConceptQueryAtom cqa){
		
		if(cqa != null){
			NamedPredicate np = cqa.getNamedPredicate();
			URI uri = np.getUri();
			String prefix = coupler.getPrefixForUri(uri);
			return prefix+":"+uri.getFragment();
		}else{
			return "";
		}
	}
	
	public String getFunctionName(FunctionTerm ft){
		URI uri = ft.getURI();
		String prefix = coupler.getPrefixForUri(uri);
		return prefix+":"+uri.getFragment();
	}
	
	public void setCoupler(APICoupler coupler){
		this.coupler = coupler;
	}
}
