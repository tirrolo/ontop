package it.unibz.krdb.obda.owlrefplatform.dav.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics {
	// SET THIS TO TRUE IF YOU WANT TO ENABLE STATISTICS
	private static final boolean active = true;
	private static boolean noExpensiveStats = true;
	
	private static Map<String, SimpleStatistics> mStats = new HashMap<String, SimpleStatistics>();
	private static String curLabel;
	
	private static long wastedTime = 0;
	
	public static void resetWastedTime(){
		wastedTime = 0;
	}
	
	public static long getWastedTime(){
		return wastedTime;
	}
	
	public static void setLabel(String label){
		if( !active ) return; 
		
		curLabel = label;
	}
	
	public static String getLabel(){
		return curLabel;
	}
	
	public static void addTime(String label, String key, long increment){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).addTime(key, increment);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.addTime(key, increment);
			mStats.put(label, stat);
		}
	}
	public static void setBoolean(String label, String key, boolean value){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).setBoolean(key, value);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.setBoolean(key, value);
			mStats.put(label, stat);
		}		
	}
	public static void setTime(String label, String key, long value){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).setTime(key, value);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.setTime(key, value);
			mStats.put(label, stat);
		}		
	}
	public static void setInt(String label, String key, int value){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).setInt(key, value);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.setInt(key, value);
			mStats.put(label, stat);
		}		
	}
	
	public static void setFloat(String label, String key, float value){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).setFloat(key, value);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.setFloat(key, value);
			mStats.put(label, stat);
		}	
	}
	
	public static void addInt(String label, String key, int increment){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).addInt(key, increment);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.addInt(key, increment);
			mStats.put(label, stat);
		}
	}
	
	public static void addFloat(String label, String key, float increment){
		if( !active ) return; 
		if( mStats.containsKey(label) ){
			mStats.get(label).addFloat(key, increment);
		}
		else{
			SimpleStatistics stat = new SimpleStatistics();
			stat.setGlobalLabel(label);
			stat.addFloat(key, increment);
			mStats.put(label, stat);
		}
	}
	
	public static String printStats(){
		
		StringBuilder result = new StringBuilder();
		
		for( String label : mStats.keySet() ){
			result.append(mStats.get(label).printStats());
		}
		
		return result.toString();
	}
	
	public static void reset(){
		mStats.clear();
		System.gc();
	}
	
	public static void expensiveStatsOff(){
		noExpensiveStats = true;
	}
	
	public static void expensiveStatsOn(){
		noExpensiveStats = false;
	}
	
	public static void fillStatisticsDLogProgMappings(DatalogProgram program, String title){

		if( noExpensiveStats ) return;
		
		long startTime = System.currentTimeMillis();
		
		Statistics.setInt(Statistics.getLabel(), title+"_originalSizeMappingAssertions", program.getRules().size());
		
		int mappedTerm = 0;
		for( Predicate head : program.getHeads() ){
			
			int n_rule = 0;
			for( CQIE rule : program.getRules(head) ){
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_exVars_for_term_"+mappedTerm, 0);
				Statistics.setInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_totVars_for_term_"+mappedTerm, 0);
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_binary_for_term_"+mappedTerm, 0);
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_unary_for_term_"+mappedTerm, 0);
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nBodyAtoms_for_term_"+mappedTerm, 0);
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nJoin_for_term_"+mappedTerm, 0);
				List<String> headVarnames = new ArrayList<String>();
				for( Variable v : rule.getHead().getVariables() ){
					headVarnames.add(v.getName());
				}
				List<String> exVarNames = new ArrayList<String>();
				Statistics.setInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_totVars_for_term_"+mappedTerm, rule.getVariableCount().keySet().size() );
				for( Function f : rule.getBody() ){
					if( f.getArity() == 2 ) Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_binary_for_term_"+mappedTerm, 1);
					else if( f.getArity() == 1 ) Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_unary_for_term_"+mappedTerm, 1);
					Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nBodyAtoms_for_term_"+mappedTerm, 1);
					
					// Number of existential variables
					for( Variable v : f.getVariables() )
						if( !headVarnames.contains(v.getName()) && !exVarNames.contains(v.getName())){ 
							exVarNames.add(v.getName());
							Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_exVars_for_term_"+mappedTerm, 1);
						}
				}
				// Number of Join variables
				int nJoins = 0;
				for( Variable v : rule.getVariableCount().keySet() ){
					if( headVarnames.contains(v.getName()) ){
						int count = rule.getVariableCount().get(v);
						nJoins = count > 2 ? count - 2 : nJoins;
					}
					else{
						int count = rule.getVariableCount().get(v);
						nJoins = count > 1 ? count - 1 : nJoins;
					}
				}
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nJoin_for_term_"+mappedTerm, nJoins); // No dup occ of preds in rules
				++n_rule;
			}
			Statistics.setInt(Statistics.getLabel(), title+"_num_mappings_for_term_"+(mappedTerm++), n_rule);
		}
		
		long endTime = System.currentTimeMillis();
		
		wastedTime += endTime - startTime;
	}
	
	/**
	 * @author Davide Lanti
	 * @param prog
	 * @param title
	 */
	public static void fillStatisticsDLogProg(DatalogProgram program, String title){

		if( noExpensiveStats ) return;
		
		long startTime = System.currentTimeMillis();
		
		Statistics.setInt(Statistics.getLabel(), title+"_n_datalog_rules", program.getRules().size());
		Statistics.setBoolean(Statistics.getLabel(), title+"_n_isUCQ", program.isUCQ());

		int n_rule = 0;
		for( CQIE rule : program.getRules() ){
			// INIT
			Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_exVars", 0);
			Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nBodyAtoms", 0);
			Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_unary", 0);
			Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_binary", 0);
			Statistics.setInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_totVars", 0 );
			Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nJoin", 0);
			List<String> headVarnames = new ArrayList<String>();
			for( Variable v : rule.getHead().getVariables() ){
				headVarnames.add(v.getName());
			}
			List<String> exVarNames = new ArrayList<String>();
			Statistics.setInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_totVars", rule.getVariableCount().keySet().size() );
			for( Function f : rule.getBody() ){
				if( f.getArity() == 2 ) Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_binary", 1);
				else if( f.getArity() == 1 ) Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_unary", 1);
				Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nBodyAtoms", 1);
				
				// Number of existential variables
				for( Variable v : f.getVariables() )
					if( !headVarnames.contains(v.getName()) && !exVarNames.contains(v.getName())){ 
						exVarNames.add(v.getName());
						Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_exVars", 1);
					}
			}
			// Number of Join variables
			int nJoins = 0;
			for( Variable v : rule.getVariableCount().keySet() ){
				if( headVarnames.contains(v.getName()) ){
					int count = rule.getVariableCount().get(v);
					nJoins = count > 2 ? count - 2 : nJoins;
				}
				else{
					int count = rule.getVariableCount().get(v);
					nJoins = count > 1 ? count - 1 : nJoins;
				}
			}
			Statistics.addInt(Statistics.getLabel(), title+"_dlog_rule_"+n_rule+"_nJoin", nJoins); // No dup occ of preds in rules
			++n_rule;
		}
		Statistics.setInt(Statistics.getLabel(), title+"_dlog_nRules", n_rule);
		
		long endTime = System.currentTimeMillis();
		wastedTime += endTime - startTime;
	}
}
