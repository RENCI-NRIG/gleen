/**
 * Copyright 2008 University of Washington Structural Informatics Group
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
package edu.washington.sig.gleen.path;

import java.util.ArrayList;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Todd Detwiler
 * @date Apr 24, 2008
 *
 * A Path is an ordered list of Triples
 */
public class Path extends ArrayList<Triple>
{
	/**
	 * flag used in path joins (see PathUtils) that allows this path to be joined with any 
	 * other path regardless of its head or tail
	 */
	private boolean matchesAny = false;
	
	public Path()
	{
		super();
	}
	
	public Path(Path inPath)
	{
		super(inPath);
	}
	
	/**
	 * Get the last node on a path
	 * @return last node on path
	 */
	public Node getPathTail()
	{
		Node tail = null;
		if(!this.isEmpty())
		{
			tail = this.get(this.size()-1).getObject();
		}
		return tail;
	}
	
	/**
	 * Get the first node on a path
	 * @return first node on path
	 */
	public Node getPathHead()
	{
		Node head = null;
		if(!this.isEmpty())
		{
			head = this.get(0).getSubject();
		}
		return head;
	}

	/**
	 * @return the value of matchesAny
	 */
	public boolean isMatchesAny()
	{
		return matchesAny;
	}

	/**
	 * @param matchesAny value to set this.matchesAny flag
	 */
	public void setMatchesAny(boolean matchesAny)
	{
		this.matchesAny = matchesAny;
	}
}
