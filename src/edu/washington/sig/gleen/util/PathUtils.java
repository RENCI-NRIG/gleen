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

package edu.washington.sig.gleen.util;

import java.util.HashSet;
import java.util.Set;

import org.biodiag.util.HashMultiValueMap;

import org.apache.jena.graph.Node;

import edu.washington.sig.gleen.path.Path;

/**
 * @author Todd Detwiler
 * @date Apr 24, 2008
 *
 * This class contains utility functions for operating on Paths.
 */
public class PathUtils
{
	/**
	 * Create a new set of paths by joining head paths and tail paths such that,
	 * paths are joined when the last node on a head path is equivalent to the 
	 * first node on the associated tail path.
	 * @param headPaths a set of path beginnings
	 * @param tailPaths a set of path endings
	 * @return a new set of complete paths
	 */
	public static Set<Path> joinPaths(Set<Path> headPaths, Set<Path> tailPaths)
	{
		Set<Path> resultPaths = new HashSet<Path>();
		HashMultiValueMap<Node, Path> tail2PathsMap = new HashMultiValueMap<Node, Path>();
		
		// build tail2PathsMap
		for(Path currPath : headPaths)
		{
			if(currPath.isMatchesAny())
			{
				resultPaths.addAll(tailPaths);
				continue;
			}
			tail2PathsMap.put(currPath.getPathTail(), currPath);
		}
		
		for(Path currPath : tailPaths)
		{
			if(currPath.isMatchesAny())
			{
				resultPaths.addAll(headPaths);
				continue;
			}
			
			Node headNode = currPath.getPathHead();
			
			// get all headPaths that end in this node
			Set<Path>  matchingHeadPaths = tail2PathsMap.get(headNode);
			for(Path currMatchHeadPath : matchingHeadPaths)
			{
				Path currResultPath = new Path(currMatchHeadPath);
				currResultPath.addAll(currPath);
				resultPaths.add(currResultPath);
			}
		}
		
		return resultPaths;
	}
}
