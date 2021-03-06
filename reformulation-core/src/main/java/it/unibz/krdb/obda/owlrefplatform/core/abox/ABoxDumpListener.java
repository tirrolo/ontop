package it.unibz.krdb.obda.owlrefplatform.core.abox;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.OBDADataSource;

/**
 * A simple listener interface that notifies its classes when a 
 * abox dump was succesfull
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface ABoxDumpListener {

	/**
	 * Notifies that a abox dump was successful
	 * 
	 * @param ds the data source to which the dump was made
	 */
	public void dump_successful(OBDADataSource ds);
}
