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
package inf.unibz.it.obda.rdbmsgav.domain;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;


public class RDBMSSQLQuery extends SourceQuery {
	//String	sql_string	= null;

	public RDBMSSQLQuery(String sql_query, APIController apic) throws QueryParseException {
		super(sql_query, apic);
		//this.sql_string = sql_query;
	}
	
	public RDBMSSQLQuery() throws QueryParseException {
		super();
		//this.sql_string = sql_query;
	}

	// public static RDBMSSQLQuery getFromString(String sql_query) {
	// return new RDBMSSQLQuery(sql_query);
	// }

	public String toString() {
		if ((this.inputquery == null) || (inputquery.equals(""))) {
			return "";
		}
		return inputquery;
	}

	public RDBMSSQLQuery clone() {
		RDBMSSQLQuery clone = null;
		try {
			clone = new RDBMSSQLQuery(new String(inputquery), apic);
		} catch (QueryParseException e) {
			
		}
		return clone;
	}

	@Override
	public RDBMSSQLQuery parse(String query, APIController apic) throws QueryParseException {
		return new RDBMSSQLQuery(query, apic);
	}

	@Override
	protected void updateObjectToInputQuery(APIController apic) {
		//Does nothing, toString already uses the same value as

	}

	@Override
	public int hashCode(){
		return this.toString().hashCode();
	}
}
