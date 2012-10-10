/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.dataquery.panel;

import inf.unibz.it.obda.gui.swing.action.GetDefaultSPARQLPrefixAction;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

// import edu.stanford.smi.protegex.owl.model.OWLModel;

// import com.hp.hpl.jena.query.*;

// import com.hp.hpl.jena.graph.query.Query;

public class SPARQLQueryStyledDocument extends DefaultStyledDocument implements DocumentListener {

	private static final long				serialVersionUID	= -4291908267565566128L;

	private boolean							alreadyColoring		= false;
	public StyleContext						context				= null;
	SPARQLQueryStyledDocument				myself				= this;
	public Style							default_style		= null;

	private GetDefaultSPARQLPrefixAction	_getPrefixAction	= null;

	// SPARQLParser parser = null;

	public SPARQLQueryStyledDocument(StyleContext context) {
		this(context, null);
	}

	public SPARQLQueryStyledDocument(StyleContext context, GetDefaultSPARQLPrefixAction action) {
		super(context);
		this._getPrefixAction = action;

		default_style = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(default_style, "Courier");
		StyleConstants.setFontSize(default_style, 12);
		StyleConstants.setForeground(default_style, Color.gray);

		try {
			insertString(0, "SELECT $x WHERE { $x  }", default_style);
		} catch (BadLocationException e) {
			e.printStackTrace(System.err);
		}
		addDocumentListener(this);
	}

	public void setGetDefaultSPARQLPrefixAction(GetDefaultSPARQLPrefixAction action) {
		_getPrefixAction = action;
	}

	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		// recolorQuery();
	}

	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		if (alreadyColoring)
			return;
		recolorQuery();
	}

	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		if (alreadyColoring)
			return;
		recolorQuery();
	}

	private void recolorQuery() {

		alreadyColoring = true;
		String input = null;
		boolean invalid = false;
		Query query = null;

		try {
			input = getText(0, getLength());
			_getPrefixAction.run();

			String prefix = (String) _getPrefixAction.getResult();
			query = QueryFactory.create(prefix + input);

		} catch (Exception e) {
			invalid = true;
//			System.out.println(e.getMessage());
		}

		if ((invalid) || (!(query.isSelectType() || query.isAskType()))) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						removeDocumentListener(myself);
						SimpleAttributeSet attributes = new SimpleAttributeSet();
//						attributes.
						attributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED);
						setCharacterAttributes(0, getLength(), attributes, true);
						addDocumentListener(myself);

					} catch (Exception e) {
						System.err.print("Unexcpected error: " + e.getMessage());
						e.printStackTrace(System.err);
					}
				}
			});
		} else {
			final Query current_query = query;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						int size = 14;
						removeDocumentListener(myself);
						String input = getText(0, getLength());

						// SimpleAttributeSet attributes = new
						// SimpleAttributeSet();
						// attributes.addAttribute(StyleConstants.CharacterConstants.Foreground,
						// Color.black);
						SimpleAttributeSet black = new SimpleAttributeSet();
						
						black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
						StyleConstants.setFontSize(black, size);

						// StyleConstants.setFontSize(default_style, 12);
						 StyleConstants.setFontFamily(default_style, "Arial");
						// StyleConstants.setBold(default_style, Boolean.FALSE);
						StyleConstants.setFontSize(default_style, size);

						setCharacterAttributes(0, input.length(), default_style, Boolean.TRUE);

						SimpleAttributeSet keyword_styles = new SimpleAttributeSet();
						// keyword_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						StyleConstants.setFontSize(keyword_styles, size);

						keyword_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED.darker());

						SimpleAttributeSet bracket_styles = new SimpleAttributeSet();
						StyleConstants.setFontSize(bracket_styles, size);

						bracket_styles.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
						bracket_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);

						SimpleAttributeSet variables_styles = new SimpleAttributeSet();
						// variables_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						StyleConstants.setFontSize(variables_styles, size);

						variables_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.green.darker());

						SimpleAttributeSet predicates_styles = new SimpleAttributeSet();
						// variables_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						StyleConstants.setFontSize(predicates_styles, size);

						predicates_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.blue.brighter().brighter());

						SimpleAttributeSet rdf_style = new SimpleAttributeSet();
						// variables_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						StyleConstants.setFontSize(rdf_style, size);

						rdf_style.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.cyan.darker());

						SimpleAttributeSet classes_styles = new SimpleAttributeSet();
						// variables_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						StyleConstants.setFontSize(classes_styles, size);

						classes_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.YELLOW.darker());

						SimpleAttributeSet constants_styles = new SimpleAttributeSet();
						// variables_styles.addAttribute(StyleConstants.CharacterConstants.Bold,
						// Boolean.TRUE);
						StyleConstants.setFontSize(constants_styles, size);
						constants_styles.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.magenta.darker());

						SimpleAttributeSet attributes = new SimpleAttributeSet();
						attributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED);
						setCharacterAttributes(0, getLength(), keyword_styles, true);

						int pos = input.indexOf("{", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, bracket_styles, false);
							pos = input.indexOf("{", pos + 1);
						}
						pos = input.indexOf("}", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, bracket_styles, false);
							pos = input.indexOf("}", pos + 1);
						}
						pos = input.indexOf(".", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, keyword_styles, false);
							pos = input.indexOf(".", pos + 1);
						}
						pos = input.indexOf("*", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, 1, variables_styles, false);
							pos = input.indexOf(".", pos + 1);
						}

						pos = input.indexOf("rdf:type", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, "rdf:type".length(), rdf_style, false);
							pos = input.indexOf("rdf:type", pos + 1);
						}

						List sel_vars = current_query.getResultVars();
						List sel_uris = current_query.getResultURIs();
						ArrayList<Node_URI> predicates = new ArrayList<Node_URI>();
						ArrayList<Node_Literal> concepts = new ArrayList<Node_Literal>();
						ArrayList<Node_Literal> constants = new ArrayList<Node_Literal>();

						com.hp.hpl.jena.sparql.syntax.Element pattern = current_query.getQueryPattern();
						ElementGroup group = (ElementGroup) pattern;
						List list = group.getElements();

						for (int k = 0; k < list.size(); k++) {

							ElementGroup current_group = null;
							ElementTriplesBlock triplesBock = null;

							if (list.get(k) instanceof ElementGroup) {
								current_group = (ElementGroup) list.get(k);
								triplesBock = (ElementTriplesBlock) current_group.getElements().get(0);
							} else if (list.get(k) instanceof ElementTriplesBlock) {
								// current_group = (ElementGroup) list.get(k);
								triplesBock = (ElementTriplesBlock) list.get(0);
							} else if (list.get(k) instanceof ElementFilter) {
								continue;
							}

							BasicPattern triples = triplesBock.getTriples();
							for (int i = 0; i < triples.size(); i++) {
								Triple triple = triples.get(i);
								Node o = triple.getObject();
								Node p = triple.getPredicate();
								Node s = triple.getSubject();

								if (p instanceof Node_URI) {
									Node_URI predicate = (Node_URI) p;
									if (predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
										concepts.add((Node_Literal) o);
									} else {
										predicates.add((Node_URI) p);
										if (o instanceof Node_Literal) {
											constants.add((Node_Literal) o);
										} else if (o instanceof Var) {
											sel_vars.add(((Var) o).getName());
										}
									}
									if (s instanceof Node_Literal) {
										constants.add((Node_Literal) s);
									} else if (s instanceof Var) {
										sel_vars.add(((Var) s).getName());
									}
								}
							}
						}

						Iterator var_it = sel_vars.iterator();
						while (var_it.hasNext()) {
							String var = (String) var_it.next();
							int x = input.indexOf(var, 0);
							while (x != -1) {
								if ((input.charAt(x - 1) == '?') || (input.charAt(x - 1) == '$')) {
									setCharacterAttributes(x - 1, var.length() + 1, variables_styles, false);
								}
								x = input.indexOf(var.toString(), x + 1);
							}
						}

						Iterator<Node_URI> pred_it = predicates.iterator();
						while (pred_it.hasNext()) {
							Node_URI pred = pred_it.next();
							int x = input.indexOf(pred.getLocalName().toString(), 0);
							while (x != -1) {
								if (input.charAt(x - 1) == ':') {
									setCharacterAttributes(x - 1, pred.getLocalName().toString().length() + 1, predicates_styles, false);
								}
								x = input.indexOf(pred.getLocalName().toString(), x + 1);
							}
						}
						//
						Iterator<Node_Literal> const_it = constants.iterator();
						while (const_it.hasNext()) {
							Node_Literal constant = const_it.next();
							int x = input.indexOf(constant.getLiteralValue().toString(), 0);
							while (x != -1) {
								if ((input.charAt(x - 1) == '\'') || (input.charAt(x - 1) == '\"')) {
									setCharacterAttributes(x, constant.getLiteralValue().toString().length(), constants_styles, false);
								}
								x = input.indexOf(constant.getLiteralValue().toString(), x + 1);
							}
						}
						//
						Iterator<Node_Literal> classes_it = concepts.iterator();
						while (classes_it.hasNext()) {
							Node_Literal concept = classes_it.next();
							int x = input.indexOf(concept.getLiteralValue().toString(), 0);
							while (x != -1) {
								try {
									if ((input.charAt(x - 1) == '\'') || (input.charAt(x - 1) == '\"')) {
										setCharacterAttributes(x, concept.getLiteralValue().toString().length(), classes_styles, false);
									}
									x = input.indexOf(concept.getLiteralValue().toString(), x + 1);
								} catch (StringIndexOutOfBoundsException e) {
									throw e;
									//return;
								}
							}
						}

						pos = input.indexOf("rdf:type", 0);
						while (pos != -1) {
							setCharacterAttributes(pos, "rdf:type".length(), rdf_style, false);
							pos = input.indexOf("rdf:type", pos + 1);
						}
						//
						// pos = input.indexOf("SELECT", 0);
						// setCharacterAttributes(pos, "SELECT".length(),
						// keyword_styles, false);
						// pos = input.indexOf("WHERE", 0);
						// setCharacterAttributes(pos, "WHERE".length(),
						// keyword_styles, false);

						addDocumentListener(myself);

					} catch (Exception e) {
						System.err.print("Unexcpected error: " + e.getMessage());
						e.printStackTrace(System.err);
						addDocumentListener(myself);
					}
				}
			});
		}
		alreadyColoring = false;
	}

	/**
	 * Creates the SPARQL PREFIX and BASE declarations for a given OWLModel.
	 * This string can be used to start a new query.
	 * 
	 * @param owlModel
	 *            the OWLModel to get the prefixes of
	 * @return a prefix declaration string
	 */
	// private String createPrefixDeclarations(OWLModel owlModel) {
	// String queryString = "";
	// String defaultNamespace =
	// owlModel.getNamespaceManager().getDefaultNamespace();
	// if (defaultNamespace.endsWith("#")) {
	// queryString += "BASE <" + defaultNamespace.substring(0,
	// defaultNamespace.length() - 1) + ">\n";
	// } else {
	// queryString += "BASE <" + defaultNamespace + ">\n";
	// }
	// queryString += "PREFIX : <" + defaultNamespace + ">\n";
	// Iterator prefixes =
	// owlModel.getNamespaceManager().getPrefixes().iterator();
	// while (prefixes.hasNext()) {
	// String prefix = (String) prefixes.next();
	// String namespace =
	// owlModel.getNamespaceManager().getNamespaceForPrefix(prefix);
	// queryString += "PREFIX " + prefix + ": <" + namespace + ">\n";
	// }
	// return queryString;
	// }
}