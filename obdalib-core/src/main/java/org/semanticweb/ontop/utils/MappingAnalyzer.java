package org.semanticweb.ontop.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDASQLQuery;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.parser.SQLQueryTranslator;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.DataDefinition;
import org.semanticweb.ontop.sql.api.RelationJSQL;
import org.semanticweb.ontop.sql.api.SelectionJSQL;
import org.semanticweb.ontop.sql.api.VisitedQuery;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;

//import com.hp.hpl.jena.iri.IRI;

public class MappingAnalyzer {

	private List<OBDAMappingAxiom> mappingList;
	private DBMetadata dbMetaData;

	private SQLQueryTranslator translator;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 */
	public MappingAnalyzer(List<OBDAMappingAxiom> mappingList, DBMetadata dbMetaData) {
		this.mappingList = mappingList;
		this.dbMetaData = dbMetaData;

		translator = new SQLQueryTranslator(dbMetaData);
	}

	public DatalogProgram constructDatalogProgram() {
		DatalogProgram datalog = dfac.getDatalogProgram();
		LinkedList<String> errorMessage = new LinkedList<String>();
		for (OBDAMappingAxiom axiom : mappingList) {
			try {
				// Obtain the target and source query from each mapping axiom in
				// the model.
				CQIE targetQuery = (CQIE) axiom.getTargetQuery();
				
				// This is the new way to get the parsed sql, since it is already parsed by the mapping parser
				// Currently disabled, to prevent interference with the MetaMappingExpander
				//QueryTree queryTree = axiom.getSourceQueryTree();
				
				OBDASQLQuery sourceQuery = (OBDASQLQuery) axiom.getSourceQuery();

				// Construct the SQL query tree from the source query
				VisitedQuery queryParsed = translator.constructParser(sourceQuery.toString());
								
				// Create a lookup table for variable swapping
				LookupTable lookupTable = createLookupTable(queryParsed);

				// We can get easily the table from the SQL query tree
				ArrayList<RelationJSQL> tableList = queryParsed.getTableSet();

				// Construct the body from the source query
				ArrayList<Function> atoms = new ArrayList<Function>();
				for (RelationJSQL table : tableList) {
					// Construct the URI from the table name
					String tableName = table.getGivenName();
					String predicateName = tableName;

					// Construct the predicate using the table name
					int arity = dbMetaData.getDefinition(tableName).countAttribute();
					Predicate predicate = dfac.getPredicate(predicateName, arity);

					// Swap the column name with a new variable from the lookup table
					List<Term> terms = new ArrayList<Term>();
					for (int i = 1; i <= arity; i++) {
						String columnName = dbMetaData.getFullQualifiedAttributeName(tableName, table.getAlias(), i);
						String termName = lookupTable.lookup(columnName);
						if (termName == null) {
							throw new RuntimeException("Column '" + columnName + "'was not found in the lookup table: ");
						}
						Term term = dfac.getVariable(termName);
						terms.add(term);
					}
					// Create an atom for a particular table
					Function atom = dfac.getFunction(predicate, terms);
					atoms.add(atom);
				}

				// For the join conditions WE STILL NEED TO CONSIDER NOT EQUI JOIN
				ArrayList<String> joinConditions =  queryParsed.getJoinCondition();
				for (String predicate : joinConditions) 
				{
					
					String[] value = predicate.split("=");
					String leftValue = value[0].trim();
					String rightValue = value[1].trim();
					
					String lookup1 = lookupTable.lookup(leftValue);
					String lookup2 = lookupTable.lookup(rightValue);
					if (lookup1 == null)
						throw new RuntimeException("Unable to get column name for variable: " + leftValue);
					if (lookup2 == null)
						throw new RuntimeException("Unable to get column name for variable: " + rightValue);

					Term t1 = dfac.getVariable(lookup1);
					Term t2 = dfac.getVariable(lookup2);

					Function atom = dfac.getFunctionEQ(t1, t2);
					atoms.add(atom);
				}

				// For the selection "where" clause conditions
				SelectionJSQL selection = queryParsed.getSelection();
				if (selection!=null) {
					
					// Stack for filter function
					Stack<Function> filterFunctionStack = new Stack<Function>();
					
					Expression conditions = selection.getRawConditions();
				
					Object element = conditions;
					if (element instanceof BinaryExpression) {
						BinaryExpression pred = (BinaryExpression) element;
					
						Function filterFunction = getFunction(pred, lookupTable);
						
						filterFunctionStack.push(filterFunction);

					} else if (element instanceof IsNullExpression) {
						IsNullExpression pred = (IsNullExpression) element;
						Function filterFunction = getFunction(pred, lookupTable);
						filterFunctionStack.push(filterFunction);
					
						
					} else if (element instanceof Parenthesis) {
						Parenthesis pred = (Parenthesis) element;
	
						Function filterFunction = manageParenthesis(pred,lookupTable);
						filterFunctionStack.push(filterFunction);
						}
						

					// The filter function stack must have 1 element left
					if (filterFunctionStack.size() == 1) {
						Function filterFunction = filterFunctionStack.pop();
						Function atom = dfac.getFunction(filterFunction.getFunctionSymbol(), filterFunction.getTerms());
						atoms.add(atom);
					} else {						
						throwInvalidFilterExpressionException(filterFunctionStack);
					}
					
				
				}
		
				
				// Construct the head from the target query.
				List<Function> atomList = targetQuery.getBody();
				//for (Function atom : atomList) {
				Iterator<Function> atomListIter = atomList.iterator();
				
				while(atomListIter.hasNext())
				{
					Function atom = atomListIter.next();
					List<Term> terms = atom.getTerms();
					List<Term> newterms = new LinkedList<Term>();
					for (Term term : terms) {
						newterms.add(updateTerm(term, lookupTable));
					}
					Function newhead = dfac.getFunction(atom.getPredicate(), newterms);
					CQIE rule = dfac.getCQIE(newhead, atoms);
					datalog.appendRule(rule);
				}
				
			} catch (Exception e) {
				errorMessage.add("Error in mapping with id: " + axiom.getId() + " \n Description: "
						+ e.getMessage() + " \nMapping: [" + axiom.toString() + "]");
				
			}
		}
		
		if (errorMessage.size() > 0) {
			StringBuilder errors = new StringBuilder();
			for (String error: errorMessage) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error analyzing the following mappings. Please correct the issue(s) to continue.\n" + errors.toString();
			RuntimeException r = new RuntimeException(msg);
			throw r;
		}
		return datalog;
	}
	
	private void throwInvalidFilterExpressionException(Stack<Function> filterFunctionStack) {
		StringBuilder filterExpression = new StringBuilder();
		while (!filterFunctionStack.isEmpty()) {
			filterExpression.append(filterFunctionStack.pop());
		}
		throw new RuntimeException("Illegal filter expression: " + filterExpression.toString());
	}
	
	/**
	 * Used to retrieve the expression contained in the parenthesis. Call getFunction 
	 * @param paren 
	 * @param lookupTable
	 * @return a function from the OBDADataFactory
	 */
	private Function manageParenthesis(Parenthesis paren, LookupTable lookupTable){
		Expression inside = paren.getExpression();
		
		if (inside instanceof BinaryExpression){
			BinaryExpression insideB= (BinaryExpression) inside;
			return getFunction(insideB, lookupTable);
		}
		
		throw new RuntimeException("Empty or irregular parenthesis: " + paren);
	}
	
	/**
	 * Methods to create a {@link Function} starting from a {@link IsNullExpression}
	 * @param pred IsNullExpression
	 * @param lookupTable
	 * @return a function from the OBDADataFactory
	 */
	
	private Function getFunction(IsNullExpression pred, LookupTable lookupTable) {
		
		Expression column = pred.getLeftExpression();
		String columnName = column.toString();
		String variableName = lookupTable.lookup(columnName);
		if (variableName == null) {
			throw new RuntimeException("Unable to find column name for variable: " + columnName);
		}
		Term var = dfac.getVariable(variableName);

		if (!pred.isNot()) {
			return dfac.getFunctionIsNull(var);
		} else {
			return dfac.getFunctionIsNotNull(var);
		}
	}

	/**
	 *  Recursive methods to create a {@link Function} starting from a {@link BinaryExpression}
	 *  We consider all possible values of the left and right expressions
	 * @param pred
	 * @param lookupTable
	 * @return
	 */

	private Function getFunction(BinaryExpression pred, LookupTable lookupTable) {
		Expression left = pred.getLeftExpression();
		Expression right = pred.getRightExpression();
		
		String leftValueName = left.toString();
		String termLeftName = lookupTable.lookup(leftValueName);
		Term t1=null;
		
		if (termLeftName == null) {
			if(left instanceof BinaryExpression)
				t1=getFunction((BinaryExpression) left, lookupTable);
			else if (left instanceof IsNullExpression)
				t1=getFunction((IsNullExpression) left, lookupTable);
			else if (left instanceof Parenthesis){
				t1= manageParenthesis((Parenthesis) left, lookupTable);
			}
			else
			throw new RuntimeException("Unable to find column name for variable: " + leftValueName);
		}
		else
		{
			t1 = dfac.getVariable(termLeftName);
		}
		
		String termRightName = "";
		Term t2 = null;
		
		if(right instanceof BinaryExpression)
			t2=getFunction((BinaryExpression) right, lookupTable);
		else if (right instanceof IsNullExpression)
			t2=getFunction((IsNullExpression) right, lookupTable);
		else if (right instanceof Parenthesis)
			t2= manageParenthesis((Parenthesis) right, lookupTable);
		else
			if (right instanceof Column) {
				//if the columns contains a boolean value
				String rightValueName = ((Column) right).getColumnName();
				if(rightValueName.toLowerCase().equals("true") || rightValueName.toLowerCase().equals("false"))
					t2 = dfac.getConstantLiteral(rightValueName, COL_TYPE.BOOLEAN);
				
				else{
					
					termRightName = lookupTable.lookup(right.toString());
					if (termRightName == null) {
						throw new RuntimeException("Unable to find column name for variable: " + rightValueName);
					}
					t2 = dfac.getVariable(termRightName);
				}
			} else 
			
				if (right instanceof StringValue) {
					termRightName= ((StringValue) right).getValue();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.STRING);
					
				}else if (right instanceof DateValue) {
					termRightName= ((DateValue) right).getValue().toString();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.DATETIME); 
						
				}else if ( right instanceof TimeValue) {
					termRightName= ((TimeValue) right).getValue().toString();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.DATETIME);
					
				}else if (right instanceof TimestampValue) {
					termRightName= ((TimestampValue) right).getValue().toString();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.DATETIME); 
					
				}else if (right instanceof LongValue) {
					termRightName= ((LongValue) right).getStringValue();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.INTEGER);
					
				} else if (right instanceof DoubleValue) {
					termRightName= ((DoubleValue) right).toString();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.DOUBLE);
					
				} else {
					termRightName= right.toString();
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.LITERAL);
					
				}

		
		String op = pred.getStringExpression();
		
		
		Function funct = null;
		if( op.equals("="))
			funct = dfac.getFunctionEQ(t1, t2);
		else if(op.equals(">"))
			funct = dfac.getFunctionGT(t1, t2); 
		else if(op.equals("<")) 
			funct = dfac.getFunctionLT(t1, t2);
		else if(op.equals(">=")) 
			funct = dfac.getFunctionGTE(t1, t2);
		else if(op.equals("<="))
			funct = dfac.getFunctionLTE(t1, t2);
		else if(op.equals("<>"))
			funct = dfac.getFunctionNEQ(t1, t2);
		else if(op.equals("AND"))
			funct = dfac.getFunctionAND(t1, t2);
		else if(op.equals("OR"))
			funct = dfac.getFunctionOR(t1, t2);
		else if(op.equals("+"))
			funct = dfac.getFunctionAdd(t1, t2);
		else if(op.equals("-"))
			funct = dfac.getFunctionSubstract(t1, t2);
		else if(op.equals("*"))
			funct = dfac.getFunctionMultiply(t1, t2);
		else
			throw new RuntimeException("Unknown opertor: " + op);
		
		return funct;
		
	}

	/**
	 * Returns a new term with the updated references.
	 */
	private Term updateTerm(Term term, LookupTable lookupTable) {
		Term result = null;
		
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			String varName = var.getName();
			String termName = lookupTable.lookup(varName);
			if (termName == null) {
				final String msg = String.format("Error in identifying column name \"%s\", please check the query source in the mappings.\nPossible reasons:\n1. The name is ambiguous, or\n2. The name is not defined in the database schema.", var);
				throw new RuntimeException(msg);
			}
			result = dfac.getVariable(termName);
			
		} 
		else if (term instanceof Function) {
			Function func = (Function) term;
			List<Term> terms = func.getTerms();
			List<Term> newterms = new LinkedList<Term>();
			for (Term innerTerm : terms) {
				newterms.add(updateTerm(innerTerm, lookupTable));
			}
			result = dfac.getFunction(func.getFunctionSymbol(), newterms);
		} 
		else if (term instanceof Constant) {
			result = term.clone();
		}
		return result;
	}

	private LookupTable createLookupTable(VisitedQuery queryParsed) {
		LookupTable lookupTable = new LookupTable();

		// Collect all the possible column names from tables.
		ArrayList<RelationJSQL> tableList = queryParsed.getTableSet();

		// Collect all known column aliases
		HashMap<String, String> aliasMap = queryParsed.getAliasMap();
		
		int offset = 0; // the index offset

		for (RelationJSQL table : tableList) {
			
			String tableName = table.getTableName();
			String tableGivenName = table.getGivenName();
			DataDefinition def = dbMetaData.getDefinition(tableGivenName);
			if (def == null) {
				 def = dbMetaData.getDefinition(tableName);
				 if (def == null) {
					 throw new RuntimeException("Definition not found for table '" + tableGivenName + "'.");
				 }
			}
			int size = def.countAttribute();

			for (int i = 1; i <= size; i++) {
				// assigned index number
				int index = i + offset;
				
				// simple attribute name
				String columnName = dbMetaData.getAttributeName(tableGivenName, i);
				
				String COLUMNNAME = columnName.toUpperCase();
				String columnname = columnName.toLowerCase();
				
				lookupTable.add(columnName, index);
				if (aliasMap.containsKey(columnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(columnName), columnName);
				}
				
				// If the column name in the select string is in lower case
				if (aliasMap.containsKey(columnname)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(columnname), columnName);
				}
				
				// If the column name in the select string is in upper case
				if (aliasMap.containsKey(COLUMNNAME)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(COLUMNNAME), columnName);
				}
				
				// attribute name with table name prefix
				String tableColumnName = tableName + "." + columnName;
				lookupTable.add(tableColumnName, index);
				if (aliasMap.containsKey(tableColumnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(tableColumnName), tableColumnName);
				}
				
				// attribute name with table given name prefix
				String givenTableColumnName = tableGivenName + "." + columnName;
				lookupTable.add(givenTableColumnName, tableColumnName);
				if (aliasMap.containsKey(givenTableColumnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(givenTableColumnName), tableColumnName);
				}
				
				// attribute name with table name prefix
				String tablecolumnname = tableName + "." + columnname;
				//lookupTable.add(tablecolumnname, tableColumnName);
				if (aliasMap.containsKey(tablecolumnname)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(tablecolumnname), tableColumnName);
				}


				// attribute name with table name prefix
				String tableCOLUMNNAME = tableName + "." + COLUMNNAME;
				//lookupTable.add(tableColumnName, columnName);
				if (aliasMap.containsKey(tableCOLUMNNAME)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(tableCOLUMNNAME), tableColumnName);
				}

				
				// full qualified attribute name
				String qualifiedColumnName = dbMetaData.getFullQualifiedAttributeName(tableGivenName, i);
				lookupTable.add(qualifiedColumnName, tableColumnName);
				if (aliasMap.containsKey(qualifiedColumnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(qualifiedColumnName), tableColumnName);
				}
				
				// full qualified attribute name using table alias
				String tableAlias = table.getAlias();
				if (tableAlias!=null) {
					String qualifiedColumnAlias = dbMetaData.getFullQualifiedAttributeName(tableGivenName, tableAlias, i);
					lookupTable.add(qualifiedColumnAlias, index);
					if (aliasMap.containsKey(qualifiedColumnAlias)) {
						lookupTable.add(aliasMap.get(qualifiedColumnAlias), qualifiedColumnAlias);
					}
					if (aliasMap.containsKey(qualifiedColumnAlias.toLowerCase())) {
						lookupTable.add(aliasMap.get(qualifiedColumnAlias.toLowerCase()), qualifiedColumnAlias);
					}

					if (aliasMap.containsKey(qualifiedColumnAlias.toUpperCase())) {
						lookupTable.add(aliasMap.get(qualifiedColumnAlias.toUpperCase()), qualifiedColumnAlias);
					}
				}
			}
			offset += size;
		}
		return lookupTable;
	}
}