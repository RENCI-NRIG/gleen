/**
 * Copyright 2007 University of Washington Structural Informatics Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.washington.sig.gleen.util;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * @author Todd Detwiler
 * @date Dec 18, 2007
 *
 * This utility class makes it easy to put the query prefix mappings into the Context where
 * they can be retrieved by the OnPath property function.
 */
public class ContextUtil
{
	private static final String QUERY_PREF_MAP = "http://sig.biostr.washington.edu/gleen#queryPrefixMap" ;
	
	/**
	 * Adds all query prefix mappings to the QueryExecution
	 * @param qe the QueryExecution where we wish to add mappings
	 * @param query the query from which we retrieve prefix mappings
	 */
	public static void setQueryPrefs(QueryExecution qe, Query query)
	{
		PrefixMapping qpm = query.getPrefixMapping();
		Context qContext = qe.getContext();
		Symbol queryPrefMapSymbol = Symbol.create(QUERY_PREF_MAP);
		qContext.put(queryPrefMapSymbol, qpm);
	} 
	
	/**
	 * quick method for getting symbol for query prefix map lookup
	 * @return Symbol to use to lookup query prefix map
	 */
	public static Symbol getQueryPrefMapSymbol()
	{
		return Symbol.create(QUERY_PREF_MAP);
	}
}
