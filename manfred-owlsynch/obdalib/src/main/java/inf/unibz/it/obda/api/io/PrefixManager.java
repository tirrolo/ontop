package inf.unibz.it.obda.api.io;

import java.net.URI;
import java.util.HashMap;

public class PrefixManager {
	
	private HashMap<URI, String> uriToPrefixMap = null;
	private HashMap<String,URI> prefixToURIMap = null;
	
	public PrefixManager (){
		uriToPrefixMap = new HashMap<URI, String>();
		prefixToURIMap = new HashMap<String, URI>();
	}
	
	public void addUri(URI uri, String prefix){
		uriToPrefixMap.put(uri, prefix);
		prefixToURIMap.put(prefix, uri);
	}
	
	public URI getURIForPrefix(String prefix){
		return prefixToURIMap.get(prefix);
	}
	
	public String getPrefixForURI(URI uri){
		return uriToPrefixMap.get(uri);
	}
	
	public HashMap<String,URI> getPrefixMap(){
		return prefixToURIMap;
	}
}
