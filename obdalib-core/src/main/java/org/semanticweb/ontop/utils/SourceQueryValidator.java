/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.semanticweb.ontop.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.sql.JDBCConnectionManager;

public class SourceQueryValidator {

	private OBDAQuery sourceQuery = null;

	private Exception reason = null;

	private JDBCConnectionManager modelfactory = null;

	private OBDADataSource source = null;

	private Statement st;

	private Connection c;

	public SourceQueryValidator(OBDADataSource source, OBDAQuery q) {
		this.source = source;
		sourceQuery = q;
	}

	public boolean validate() {
		ResultSet set = null;
		try {
			modelfactory = JDBCConnectionManager.getJDBCConnectionManager();
			c = modelfactory.getConnection(source);
			st = c.createStatement();
			set = st.executeQuery(sourceQuery.toString());
			return true;
		} catch (SQLException e) {
			reason = e;
			return false;
		} catch (Exception e) {
			reason = e;
			return false;
		} finally {
			try {
				set.close();
			} catch (Exception e) {
				// NO-OP
			}
			try {
				st.close();
			} catch (Exception e) {
				// NO-OP
			}
		}
	}

	public void cancelValidation() throws SQLException {
		st.cancel();
	}

	/***
	 * Returns the exception that cause the query to be invalid.
	 * 
	 * @return Exception that caused invalidity. null if no reason was set or if
	 *         the query is valid.
	 */
	public Exception getReason() {
		return reason;
	}
}
