/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

public class TablePrimary implements ITable {
	
	private static final long serialVersionUID = -205626125381808960L;
	
	/**
	 * The schema and table name without quotes.
	 * Used for the jdbc calls
	 */
	private String schema;
	private String tableName;
	/**
	 * The full name, exactly as given by the user, possibly with schema / database prefix and quotes
	 * Used in TableDefinition and the metadata to identify the table
	 */
	private String givenName;
	private String alias;

	/**
	 * Used when the schema is not specified and we know the name is without quotation marks
	 * (For example for generated views)
	 * @param tableName Table name without quotation marks
	 */
	public TablePrimary(String tableName) {
		this(tableName, tableName);
	}
	
	/**
	 * Used when the schema is not specified (So the table is presumably in the schema of the logged in user in the oracle case)
	 * @param tableName Without quotation marks
	 * @param givenName Exactly as given by user, possible with quotation marks
	 */
	public TablePrimary(String tableName, String givenName) {
		this("", tableName, givenName);
	}
	
	public TablePrimary(String schema, String tableName, String givenName) {
		setSchema(schema);
		setTableName(tableName);
		setGivenName(givenName);
		setAlias("");
	}

	/**
	 * 
	 * @param schema The schema (in the oracle case) or database (in postgres, mysql) without qoutation marks
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	/**
	 * 
	 * @return The schema (in the oracle case) or database (in postgres, mysql) without qoutation marks
	 */
	public String getSchema() {
		return schema;
	}
	
	/**
	 * 
	 * @param tableName The table name (without prefix and without quotation marks)
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Called from JDBCConnectionManager, value passed on to jdbc methods
	 * 
	 * @return The table name (without prefix and without quotation marks)
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 
	 * @param givenName The table name exactly as it appears in the source sql query of the mapping, possibly with prefix and quotes
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * 
	 * @return The table name exactly as it appears in the source sql query of the mapping, possibly with prefix and quotes
	 */
	public String getGivenName() {
		return givenName;
	}
	
	public void setAlias(String alias) {
		if (alias == null) {
			return;
		}
		this.alias = alias;
	}
	
	public String getAlias() {
		return alias;
	}

	@Override
	public String toString() {
		String str = "";
		if (schema != "") {
			str += schema + ".";
		}			
		str += tableName;
		if (alias != "") {
			str += " as " + alias;
		}
		return str;
	}
}
