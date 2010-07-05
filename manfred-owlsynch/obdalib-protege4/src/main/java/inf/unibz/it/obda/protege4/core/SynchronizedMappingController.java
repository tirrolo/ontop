package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.RemoveAxiom;

public class SynchronizedMappingController extends MappingController implements OWLOntologyChangeListener{

	private DatasourcesController	dscontroller			= null;
	private APIController 			apic					= null;
	
	public SynchronizedMappingController(DatasourcesController dsc,
			APIController ac) {
		super(dsc, ac);
		dscontroller = dsc;
		apic = ac;
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		
		((OWLAPICoupler)apic.getCoupler()).synchWithOntology(changes.get(0).getOntology());
		boolean replace = allRemoveAxioms(changes);
		if(replace){
			if(changes.size()>2){
				OWLOntologyChange rem = changes.get(0);
				OWLOntologyChange add = changes.get(1);
				if(rem instanceof RemoveAxiom && add instanceof AddAxiom){
					Set<OWLEntity> r =rem.getAxiom().getReferencedEntities();
					Set<OWLEntity> a =add.getAxiom().getReferencedEntities();
					Iterator<OWLEntity> it1 = r.iterator();
					Iterator<OWLEntity> it2 = a.iterator();
					String old = "";
					while(it1.hasNext()){
						String aux = it1.next().toString();
						boolean found = false;
						while(it2.hasNext() && !found){
							String aux2 = it2.next().toString();
							if(aux.equals(aux2)){
								found = true;
							}
						}
						if(!found){
							old = aux;
							break;
						}
					}
					
					it1 = r.iterator();
					it2 = a.iterator();
					String neu = "";
					while(it2.hasNext()){
						String aux = it2.next().toString();
						boolean found = false;
						while(it1.hasNext() && !found){
							String aux2 = it1.next().toString();
							if(aux.equals(aux2)){
								found = true;
							}
						}
						if(!found){
							neu = aux;
							break;
						}
					}
					if(!neu.equals("") && !old.equals("")){
						replaceAxiom(old, neu);
					}
				}
			}	
		}else{
			Set<OWLEntity> entities = getInvolvedEntities(changes);
			Iterator<OWLEntity> nit = entities.iterator();
			while(nit.hasNext()){
				OWLEntity ent = nit.next();
				if(!stillExists(ent)){
					removeAxiom(ent.toString());
				}
			}
		}
	}
	
	private boolean stillExists(OWLEntity ent){
		
		APICoupler coupler = apic.getCoupler(); 
		boolean extists = true;
		if(ent instanceof OWLClass){
			extists = coupler.isNamedConcept(ent.getURI());
		}else if(ent instanceof OWLDataProperty){
			extists = coupler.isDatatypeProperty(ent.getURI());
		}else{
			extists = coupler.isObjectProperty(ent.getURI());
		}
		
		if(extists){
			return true;
		}else{
			return false;
		}
	}
	
	private Set<OWLEntity> getInvolvedEntities(List<? extends OWLOntologyChange> changes){
		Set<OWLEntity> set = new HashSet<OWLEntity>();
		
		Iterator<? extends OWLOntologyChange> it = changes.iterator();
		while(it.hasNext()){
			OWLAxiom ch = it.next().getAxiom();
			// filter out datatypes like int, string, etc
			if(ch instanceof OWLDataPropertyRangeAxiom){
				OWLDataPropertyRangeAxiom ax = (OWLDataPropertyRangeAxiom)ch;
				Set<OWLEntity> s = ax.getReferencedEntities();
				String prop = ax.getProperty().toString();
				Iterator<OWLEntity> sit = s.iterator();
				while(sit.hasNext()){
					OWLEntity ent = sit.next();
					if(prop.equals(ent.toString())){
						set.add(ent);
					}
				}
				
			}else{
				Set<OWLEntity> entities = ch.getReferencedEntities();
				Iterator<OWLEntity> eit = entities.iterator();
				while(eit.hasNext()){
					OWLEntity ent = eit.next();
					if(!ent.toString().equals("XMLLiteral") &&!ent.toString().equals("Thing") ){
						set.add(ent);
					}
				}
			}
		}
		return set;
	}
	
	private boolean allRemoveAxioms(List<? extends OWLOntologyChange> changes){
		
		Iterator<? extends OWLOntologyChange> it = changes.iterator();
		while(it.hasNext()){
			OWLOntologyChange ch = it.next();
			if(ch instanceof AddAxiom){
				return true;
			}
		}
		return false;
		
	}
	
	
	private void removeAxiom(String name){
		
		HashMap<String, DataSource> datasources = dscontroller.getAllSources();
		Set<String> keys = datasources.keySet();
		Iterator<String> kit = keys.iterator();
		while(kit.hasNext()){
			DataSource ds = dscontroller.getDataSource(kit.next());
			ArrayList<OBDAMappingAxiom> maps = getMappings(ds.getName());
			Iterator<OBDAMappingAxiom> it = maps.iterator();
			Vector<String> mappingsToRemove = new Vector<String>();
			Map<String, ArrayList<QueryAtom>> mappingsToUpdate = new HashMap<String, ArrayList<QueryAtom>>();
			while(it.hasNext()){
				OBDAMappingAxiom map = it.next();
				ConjunctiveQuery cq = (ConjunctiveQuery) map.getTargetQuery();
				ArrayList<QueryAtom> atoms = cq.getAtoms();
				Iterator<QueryAtom> it2 = atoms.iterator();
				ArrayList<QueryAtom> newList = new ArrayList<QueryAtom>();
				boolean update = false;
				while(it2.hasNext()){
					QueryAtom atom = it2.next();
					String n = atom.getName();
					if(n.equals(name)){
						update = true;
					}else{
						newList.add(atom);
					}
				}
				if(update){
					if(newList.size()==0){
						mappingsToRemove.add(map.getId());
					}else{
						mappingsToUpdate.put(map.getId(), newList);
					}
				}
			}
			
			Iterator<String> it3 = mappingsToRemove.iterator();
			while(it3.hasNext()){
				String mapID = it3.next();
				deleteMapping(ds.getName(), mapID);
			}
			
			Iterator<String> it4 = mappingsToUpdate.keySet().iterator();
			while(it4.hasNext()){
				String key = it4.next();
				ArrayList<QueryAtom> body =mappingsToUpdate.get(key);
				ConjunctiveQuery cq = new ConjunctiveQuery();
				cq.addQueryAtom(body);
				updateMapping(ds.getName(), key, cq);
			}
		}
	}

	
	private void replaceAxiom(String old, String neu){
		
		HashMap<String, DataSource> datasources = dscontroller.getAllSources();
		Set<String> keys = datasources.keySet();
		Iterator<String> kit = keys.iterator();
		while(kit.hasNext()){
			DataSource ds = dscontroller.getDataSource(kit.next());
			ArrayList<OBDAMappingAxiom> maps = getMappings(ds.getName());
			Iterator<OBDAMappingAxiom> it = maps.iterator();
			Map<String, ArrayList<QueryAtom>> mappingsToUpdate = new HashMap<String, ArrayList<QueryAtom>>();
			while(it.hasNext()){
				OBDAMappingAxiom map = it.next();
				ConjunctiveQuery cq = (ConjunctiveQuery) map.getTargetQuery();
				ArrayList<QueryAtom> atoms = cq.getAtoms();
				Iterator<QueryAtom> it2 = atoms.iterator();
				ArrayList<QueryAtom> newList = new ArrayList<QueryAtom>();
				boolean update = false;
				while(it2.hasNext()){
					QueryAtom atom = it2.next();
					String n = atom.getName();
					if(n.equals(old)){
						update = true;
						atom.setName(neu);
						newList.add(atom);
					}else{
						newList.add(atom);
					}
				}
				if(update){
					mappingsToUpdate.put(map.getId(), newList);
				}
			}
					
			Iterator<String> it4 = mappingsToUpdate.keySet().iterator();
			while(it4.hasNext()){
				String key = it4.next();
				ArrayList<QueryAtom> body =mappingsToUpdate.get(key);
				ConjunctiveQuery cq = new ConjunctiveQuery();
				cq.addQueryAtom(body);
				updateMapping(ds.getName(), key, cq);
			}
		}
	}
}
