/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.owlrefplatform.questdb;

import java.io.Serializable;
import java.util.Properties;

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.QuestConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBConnection;

public abstract class QuestDBAbstractStore implements Serializable {

	private static final long serialVersionUID = -8088123404566560283L;

	protected Quest questInstance = null;
	protected QuestConnection questConn = null;

	protected String name;

	public QuestDBAbstractStore(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* Move to query time ? */
	public Properties getPreferences() {
		return questInstance.getPreferences();
	}

	/* Move to query time ? */
	public boolean setProperty(String key, String value) {
		return false;
	}

	public QuestDBConnection getConnection() throws OBDAException {
	//	System.out.println("getquestdbconn..");
		return new QuestDBConnection(getQuestConnection());
	}
	
	public abstract QuestConnection getQuestConnection();

}
