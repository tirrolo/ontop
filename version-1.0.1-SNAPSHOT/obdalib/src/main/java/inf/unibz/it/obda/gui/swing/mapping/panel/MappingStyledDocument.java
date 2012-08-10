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
package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class MappingStyledDocument extends DefaultStyledDocument implements
		DocumentListener {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -1541062682306964359L;
	private boolean				alreadyColoring	= false;
	public StyleContext			context			= null;
	MappingStyledDocument		myself			= this;
	public Style				default_style	= null;

	private QueryPainter painter = null;
	private APIController	apic;
	
	public MappingStyledDocument(StyleContext context, APIController apic) {
		super(context);
		this.apic = apic;

		painter = new QueryPainter(apic);
		default_style = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(default_style, "Arial");
		StyleConstants.setFontSize(default_style, 14);
		addDocumentListener(this);
	}
	
	public void changedUpdate(DocumentEvent arg0) {
	}

	public void insertUpdate(DocumentEvent arg0) {
		
		if (painter.isAlreadyColoring())
			return;
		painter.startRecoloring(this);
	}

	public void removeUpdate(DocumentEvent arg0) {
		
		if (painter.isAlreadyColoring())
			return;
		painter.startRecoloring(this);
	}

}